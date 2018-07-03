def main():
    f = open("test.txt", "r")
    text = f.read()
    f.close()
    print(text)
if __name__== "__main__":
  main()