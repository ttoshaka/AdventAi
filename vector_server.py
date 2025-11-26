from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional
import faiss
import numpy as np
import ollama
import logging

# Настройка логирования
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

##uvicorn vector_server:app --host 0.0.0.0 --port 9000 --reload
app = FastAPI()

EMBED_MODEL = "nomic-embed-text"
RERANK_MODEL = "llama3.1:8b"  # замените на свою модель

VECTOR_DIM = 768
index = faiss.IndexFlatIP(VECTOR_DIM)
texts = []


class AddChunksRequest(BaseModel):
    chunks: List[str]


class SearchRequest(BaseModel):
    query: str
    top_k: int = 5
    top_n: Optional[int] = None  # если нет значения → реранкинг не применится


@app.post("/add_chunks")
def add_chunks(req: AddChunksRequest):
    if not req.chunks:
        raise HTTPException(status_code=400, detail="No chunks provided")

    try:
        for chunk in req.chunks:
            resp = ollama.embeddings(model=EMBED_MODEL, prompt=chunk)
            vector = resp.get("embeddings") or resp.get("embedding")
            vec = np.array(vector, dtype="float32").reshape(1, -1)

            index.add(vec)
            texts.append(chunk)

        return {"status": "ok", "added_chunks": len(req.chunks)}

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


def rerank_with_llm(query: str, docs: List[str]) -> List[float]:
    scores = []

    for doc in docs:
        prompt = f"""
Оцени релевантность документа к запросу.
Запрос: {query}
Документ: {doc}

Верни одно число от 0 до 10. Где 0 - документ не имеет ничего общего с запросом, а 10 - документ полность отвечает на запрос.
В твоём ответ должно быть только чилос. Тебе запрещено присылать в ответе что-то кроме числа.
Правильные примеры:
    0
    1
    2
Неправильные примеры:
    Релевантность равна 1.
    1.
    В документе не содержится информации о запросе.
"""
        response = ollama.generate(model=RERANK_MODEL, prompt=prompt)
        text = response["response"].strip()
        logger.info("Реранк")
        logger.info(text)

        try:
            score = float(text.replace(".", ""))
        except:
            score = 0.0

        scores.append(score)

    return scores


@app.post("/search")
def search_text(req: SearchRequest):
    if not texts:
        raise HTTPException(status_code=400, detail="Нет добавленных текстов для поиска")

    try:
        # Эмбеддинг запроса
        resp = ollama.embeddings(model=EMBED_MODEL, prompt=req.query)
        qvec = resp.get("embeddings") or resp.get("embedding")
        qarr = np.array(qvec, dtype="float32").reshape(1, -1)

        # Если top_n не передан → обычный FAISS поиск
        if not req.top_n:
            logger.info("Реранкинг не запрошен")
            distances, indices = index.search(qarr, req.top_k)

            results = []
            for dist, idx in zip(distances[0], indices[0]):
                results.append({
                    "text": texts[idx],
                    "distance": float(dist)
                })

            return {"results": results}

        logger.info("Реранкинг нужен")
        # Если top_n передан → выполняем реранкинг
        distances, indices = index.search(qarr, req.top_n)
        candidates = [texts[idx] for idx in indices[0]]

        # Реранкинг через LLM
        scores = rerank_with_llm(req.query, candidates)

        # Сортировка результатов
        ranked = sorted(zip(candidates, scores), key=lambda x: x[1], reverse=True)

        # Выбираем top_k
        final = ranked[:req.top_k]

        results = [
            {"text": doc, "score": float(score)}
            for doc, score in final
        ]

        return {"results": results}

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
