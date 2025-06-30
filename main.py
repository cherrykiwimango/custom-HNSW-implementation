from fastapi import FastAPI, HTTPException
import faiss
from fastapi.responses import JSONResponse
import numpy as np
import threading
from models import UserModel
from utils import convert_to_vector

app = FastAPI()

#number of parameters being considered in the vector
dimension = 29
#main index that stores the HNSW graph
app.state.index = None
app.state.lock = threading.Lock()
app.state.user_ids = []

#create new index on app startup
@app.get("/")
def create_new_index():
  if app.state.index is None:
    app.state.index = faiss.IndexHNSWFlat(dimension, 32)
    return JSONResponse(content={"message":"Index created successfully"}, status_code=201)
  return JSONResponse(content={"message":"Index already initialized"}, status_code=200)
  
#add new node into index on user create
@app.post("/add/", response_model=UserModel)
def add_node(user: UserModel):
  if app.state.index is None:
    raise HTTPException(status_code=400, detail="Index not initialized")
  vector = convert_to_vector(user)
  vector_np = np.array(vector, dtype=np.float32).reshape(1, -1)
  if vector_np.shape[1] != app.state.index.d: #checks if the dimension of input list is correct
    raise HTTPException(status_code=400, detail="Incorrect vector dimension")
  
  #ensures conflicts don't happen
  with app.state.lock:
    app.state.index.add(vector_np)
    app.state.user_ids.append(user.id)

  return JSONResponse(content={"message": "Vector added", "total_vectors": app.state.index.ntotal}, status_code=201)

@app.get("/recommendation/{user_id}")
def fetch_n_nearest(user_id: str, n: int = 5):
  #checks
  if app.state.index is None:
    raise HTTPException(status_code=400, detail="Index not initialized")
  if app.state.index.ntotal == 0:
    raise HTTPException(status_code=404, detail="Index is empty")
  #find the stored user vector using the list
  try:
    query_vector_index = app.state.user_ids.index(user_id)
  except ValueError:
    raise HTTPException(status_code=400, detail="User not found")
  
  query_vector = app.state.index.reconstruct(query_vector_index).reshape(1, -1)
  distances, indices = app.state.index.search(query_vector, n)
  similar_users = [
    {
        "user": app.state.user_ids[i],
        "distance": float(distances[0][idx])
    }
    for idx, i in enumerate(indices[0])
  ]

  return JSONResponse(content={"matches": similar_users}, status_code=200 )


if __name__ == "__main__":
  import uvicorn
  uvicorn.run(app, host="0.0.0.0", port=8000)