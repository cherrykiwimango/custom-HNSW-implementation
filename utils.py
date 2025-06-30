import numpy as np
from models import UserModel

#demo similarity tables
lookup_vectors = {
  "language_vectors": {
    "english":    [1.0, 0.1, 0.1, 0.1, 0.1, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2],
    "malayalam":  [0.1, 1.0, 0.7, 0.6, 0.5, 0.2, 0.2, 0.1, 0.1, 0.1, 0.5],
    "tamil":      [0.1, 0.7, 1.0, 0.6, 0.6, 0.2, 0.1, 0.1, 0.1, 0.1, 0.4],
    "kannada":    [0.1, 0.6, 0.6, 1.0, 0.7, 0.3, 0.3, 0.2, 0.2, 0.2, 0.6],
    "telugu":     [0.1, 0.5, 0.6, 0.7, 1.0, 0.3, 0.3, 0.2, 0.2, 0.2, 0.4],
    "hindi":      [0.2, 0.2, 0.2, 0.3, 0.3, 1.0, 0.7, 0.6, 0.6, 0.6, 0.4],
    "marati":     [0.2, 0.2, 0.1, 0.3, 0.3, 0.7, 1.0, 0.5, 0.5, 0.6, 0.6],
    "bengali":    [0.2, 0.1, 0.1, 0.2, 0.2, 0.6, 0.5, 1.0, 0.7, 0.5, 0.3],
    "odia":       [0.2, 0.1, 0.1, 0.2, 0.2, 0.6, 0.5, 0.7, 1.0, 0.4, 0.3],
    "gujrati":    [0.2, 0.1, 0.1, 0.2, 0.2, 0.6, 0.6, 0.5, 0.4, 1.0, 0.5],
    "konkani":    [0.2, 0.5, 0.4, 0.6, 0.4, 0.4, 0.6, 0.3, 0.3, 0.5, 1.0],
  },
  "city_vectors" : {
    "delhi":     [1.0, 0.7, 0.6, 0.5, 0.3, 0.4, 0.4, 0.3, 0.5, 0.3, 0.2],
    "mumbai":    [0.7, 1.0, 0.5, 0.6, 0.5, 0.6, 0.6, 0.4, 0.6, 0.3, 0.3],
    "kolkata":   [0.6, 0.5, 1.0, 0.4, 0.3, 0.4, 0.3, 0.6, 0.5, 0.3, 0.2],
    "chennai":   [0.5, 0.6, 0.4, 1.0, 0.7, 0.6, 0.5, 0.3, 0.4, 0.2, 0.3],
    "bangalore": [0.3, 0.5, 0.3, 0.7, 1.0, 0.6, 0.5, 0.4, 0.4, 0.3, 0.4],
    "hyderabad": [0.4, 0.6, 0.4, 0.6, 0.6, 1.0, 0.6, 0.3, 0.5, 0.3, 0.3],
    "pune":      [0.4, 0.6, 0.3, 0.5, 0.5, 0.6, 1.0, 0.3, 0.4, 0.3, 0.3],
    "lucknow":   [0.3, 0.4, 0.6, 0.3, 0.4, 0.3, 0.3, 1.0, 0.5, 0.2, 0.2],
    "ahmedabad": [0.5, 0.6, 0.5, 0.4, 0.4, 0.5, 0.4, 0.5, 1.0, 0.4, 0.3],
    "kochi":     [0.3, 0.3, 0.3, 0.2, 0.3, 0.3, 0.3, 0.2, 0.4, 1.0, 0.4],
    "bhubaneswar":[0.2, 0.3, 0.2, 0.3, 0.4, 0.3, 0.3, 0.2, 0.3, 0.4, 1.0],
  },
  "gender_vectors" : {
    "male": [1.0, 0.0, 0.0],
    "female": [0.0, 1.0, 0.0],
    "other": [0.0, 0.0, 1.0],
  },
  "employment_vectors" : {
    "student": [1.0, 0.0, 0.0],
    "employed": [0.0, 1.0, 0.0],
    "unemployed": [0.0, 0.0, 1.0]
  }
}

default_lang_vector = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
default_city_vector = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]

parameter_weights = [1.0, 0.8, 0.6, 0.3, 0.9] #age, location, employment, gender, langauges
W_AGE, W_LOCATION, W_EMPLOYMENT, W_GENDER, W_LANGUAGE = parameter_weights

#helper functions
def get_vector(category: str, key: str, default: list[float]) -> list[float]:
    return lookup_vectors.get(category, {}).get(key.lower(), default)

def convert_to_vector(user: UserModel):
  gender_vector = np.array(get_vector("gender_vectors", user.gender, [0, 0, 0]), dtype=np.float32) * W_GENDER
  employment_vector = np.array(get_vector("employment_vectors", user.employment, [0, 0, 0]), dtype=np.float32) * W_EMPLOYMENT
  location_vector = np.array(get_vector("city_vectors", user.location, default_city_vector), dtype=np.float32) * W_LOCATION
  language_vector = np.array(default_lang_vector, dtype=np.float32)
  for language in user.language:
    lang_vector = np.array(get_vector("language_vectors", language, default_lang_vector), dtype=np.float32) * W_LANGUAGE
    language_vector += lang_vector
  
  normalized_age = np.array([max(0.0, min(1.0, (user.age - 18) / (70 - 18)))], dtype=np.float32) 
  final_vector = np.concatenate([normalized_age, gender_vector, employment_vector, location_vector, language_vector])
  return final_vector