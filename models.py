from typing import List
from enum import Enum
from pydantic import BaseModel

class Employment(str, Enum):
  student = "student" 
  employed = "employed" 
  unemployed = "unemployed" 

class Gender(str, Enum):
  male = "male" 
  female = "female" 
  other = "other" 

class UserModel(BaseModel):
  id: str
  age: int
  employment: Employment
  gender: Gender
  location: str
  language: List[str]