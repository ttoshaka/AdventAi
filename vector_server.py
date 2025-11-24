from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List
import faiss
import numpy as np
import ollama

##uvicorn vector_server:app --host 0.0.0.0 --port 9000 --reload
app = FastAPI()

MODEL = "nomic-embed-text"

VECTOR_DIM = 768
index = faiss.IndexFlatIP(VECTOR_DIM)
texts = []

class AddChunksRequest(BaseModel):
    chunks: List[str]
    
class AddRequest(BaseModel):
    text: str

class SearchRequest(BaseModel):
    query: str
    top_k: int = 5

@app.post("/add_chunks")
def add_chunks(req: AddChunksRequest):
    if not req.chunks:
        raise HTTPException(status_code=400, detail="No chunks provided")
    
    try:
        for chunk in req.chunks:
            # Получаем вектор для каждого чанка
            resp = ollama.embeddings(model=MODEL, prompt=chunk)
            vector = resp["embeddings"] if "embeddings" in resp else resp["embedding"]
            vec = np.array(vector, dtype="float32").reshape(1, -1)
            
            # Добавляем в FAISS и список текстов
            index.add(vec)
            texts.append(chunk)
        
        return {"status": "ok", "added_chunks": len(req.chunks)}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/search")
def search_text(req: SearchRequest):
    if not texts:
        raise HTTPException(status_code=400, detail="Нет добавленных текстов для поиска")
    try:
        resp = ollama.embeddings(model=MODEL, prompt=req.query)
        qvec = resp["embeddings"] if "embeddings" in resp else resp["embedding"]
        qarr = np.array(qvec, dtype="float32").reshape(1, -1)
        distances, indices = index.search(qarr, req.top_k)
        results = []
        for dist, idx in zip(distances[0], indices[0]):
            results.append({
                "text": texts[idx],
                "distance": float(dist)
            })
        return {"results": results}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
