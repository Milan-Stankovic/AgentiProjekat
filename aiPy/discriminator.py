import time
def main():
  time.sleep(1)
  f = open("input/input.txt", "r")
  text = f.read()
  f.close()
  print(text)
  time.sleep(3)
  f = open("output/output.txt", "w+")
  f.write("HELLO AGENT FROM DISCIMINATOR !")
  f.close()
	
	
if __name__== "__main__":
  main()