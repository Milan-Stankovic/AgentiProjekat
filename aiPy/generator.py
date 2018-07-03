import time

def main():
  time.sleep(1)
  f = open("output.txt", "w+")
  f.write("HELLO AGENT FROM GENERATOR !")
  f.close()
  time.sleep(10)
  f = open("input.txt", "r")
  text = f.read()
  f.close()
  print(text)
	
	

	
if __name__== "__main__":
  main()