if __name__ == '__main__':
    s = input()
    temp = any(i.isalnum() for i in s)
    print(temp)
    temp = any(i.isalpha() for i in s)
    print(temp)
    temp = any(i.isdigit() for i in s)
    print(temp)
    temp = any(i.islower() for i in s)
    print(temp)
    temp = any(i.isupper() for i in s)
    print(temp)
