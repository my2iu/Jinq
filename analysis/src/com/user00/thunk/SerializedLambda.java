package com.user00.thunk;

/**
 * Class that imitates the java.lang.invoke.SerlializedLambda layout
 * so that the contents can be read.
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

public class SerializedLambda implements Serializable {
    private static final long serialVersionUID = 0x6f61d0942c293685L;
    public Object[] capturedArgs;
    public String implClass;
    public String implMethodName;
    public String implMethodSignature;
    public int implMethodKind;
    
    private final static byte[] serializedLambdaString = "java.lang.invoke.SerializedLambda".getBytes(StandardCharsets.US_ASCII);
    private final static byte[] serializedLambdaStringReplacement = "com.user00.thunk.SerializedLambda".getBytes(StandardCharsets.US_ASCII);
    
    private static void substituteSerializedLambda(byte[] data)
    {
      byte[] toMatch = serializedLambdaString;
      byte[] toReplace = serializedLambdaStringReplacement;
      // Replace this with Boyer-Moore or something like that (but not too
      // important because the strings are reasonable for a dumb linear search).
      nextchar:
      for (int n = 0; n < data.length - toMatch.length; n++)
      {
        for (int i = 0; i < toMatch.length; i++)
        {
          if (data[n+i] != toMatch[i])
            continue nextchar;
        }
        // Found a match, replace it with our version.
        for (int i = 0; i < toMatch.length; i++)
          data[n+i] = toReplace[i];
        return;
      }
    }

    public static SerializedLambda extractLambda(Object lambda) throws Exception
    {
      try {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream(2000);
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(lambda);
        out.close();
        byte[] data = byteOut.toByteArray();
//        FileOutputStream fileOut = new FileOutputStream("serializedlambda");
//        fileOut.write(data);
//        fileOut.close();
        substituteSerializedLambda(data);
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
        Object o = in.readObject();
        if (o instanceof SerializedLambda)
          return (SerializedLambda)o;
        return null;
      } catch(Exception e) {
        throw new Exception(e);
      }
      
    }
}
