def main():
    
	f = open("input.txt", "r")
    text = f.read()
    f.close()
    print(text)
	
	
	time.sleep(3)
	f = open("output.txt", "w+")
    f.write("HELLO AGENT FROM DISCIMINATOR !")
    f.close()
	
	
if __name__== "__main__":
  main()