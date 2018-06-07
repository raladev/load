from glob import glob

with open("finalFile", "a") as singleFile:
    has_header = False
    for csvFile in glob("*.jtl"):
        i = 1
        for line in open(csvFile, "r"):
            if i > 1:
                singleFile.write(line)
            elif not has_header:
                singleFile.write(line)
                has_header = True
            i=i+1
        print(csvFile)   
    exit()
