- Look in `oliv.fibonacci.One.java`
  - Why doesn't the line `length = Integer.parseInt(args[0]);` require a try-catch block?  
- Compile it
  - `javac -d classes -s src/main/java src/main/java/oliv/fibonacci/*.java`
- Run it
  - `java -cp classes oliv.fibonacci.One`
- Time it
  - `time java -cp classes oliv.fibonacci.One`
- Monitor it with `jconsole`
  - `java -cp classes oliv.fibonacci.One 100`
  - `jconsole`
    ![JConsole](../../../images/jconsole.png)
    ![JConsole](../../../images/jconsole.2.png)
    ![JConsole](../../../images/jconsole.3.png)
    ![JConsole](../../../images/jconsole.4.png)
  - What do you see?
  - What's going to happen?
  - How can you fix that?
    - Increase heap size?
    - Rewrite the code?
  
- See `oliv.fibonacci.Two.java`
  - `time java -cp classes oliv.fibonacci.Two 100`
  