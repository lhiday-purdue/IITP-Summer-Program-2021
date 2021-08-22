import glob
import os

print("<<<<<<<<<<<<<<<<<image>>>>>>>>>>>>>>>>")
pathToDataset = "./dataset"
listAlljpg = []
for filename in glob.glob(f"{pathToDataset}/*.jpg"):
    basename = os.path.basename(filename)
    numIndex = int(basename[:3])
    listAlljpg.append(numIndex)
listAlljpg.sort()

print(listAlljpg)
prev_i = None
count = 0
for i, v in enumerate(listAlljpg):
    if(i == 0):
        prev_v = v
    else:
        if((prev_v + 1) != v):
            print(f"Not continuous between {prev_v} and {v}")
            count +=1
        prev_v = v
print(f"Total not continuous: {count}")

#########################################
print("<<<<<<<<<<<<<<<<<annotation>>>>>>>>>>>>>>>>")
pathToAnnotation = "./annotation"
listAllAnno = []
for filename in glob.glob(f"{pathToAnnotation}/*.txt"):
    basename = os.path.basename(filename)
    numIndex = int(basename[:3])
    listAllAnno.append(numIndex)
listAllAnno.sort()

print(listAllAnno)
prev_i = None
count = 0
for i, v in enumerate(listAllAnno):
    if(i == 0):
        prev_v = v
    else:
        if((prev_v + 1) != v):
            print(f"Not continuous between {prev_v} and {v}")
            count +=1
        prev_v = v
print(f"Total not continuous: {count}")

#######not match
setImage = set(listAlljpg)
setAnno = set(listAllAnno)

setDiffImage = setImage - setAnno
setDiffAnno = setAnno - setImage

listDiffImage = list(setDiffImage)
listDiffAnno = list(setDiffAnno)

print(f"image cannot find a match at anno: {listDiffImage}")
print(f"anno cannot find a match at image: {listDiffAnno}")