package ch.epfl.labos.iu.orm.queryll2.symbolic;

import java.util.ArrayList;

import org.jinq.rebased.org.objectweb.asm.Handle;
import org.jinq.rebased.org.objectweb.asm.Opcodes;
import org.jinq.rebased.org.objectweb.asm.Type;
import org.junit.Assert;
import org.junit.Test;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue.ComparisonValue.ComparisonOp;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue.MathOpValue.Op;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue.UnaryMathOpValue.UnaryOp;

public class TypedValueTest
{
   @Test
   public void testEquals()
   {
      Assert.assertEquals(new TypedValue(Type.INT_TYPE), new TypedValue(Type.INT_TYPE));
      Assert.assertEquals(new TypedValue.ArgValue(Type.INT_TYPE, 1), new TypedValue.ArgValue(Type.INT_TYPE, 1));
      Assert.assertEquals(
            new TypedValue.ComparisonValue(ComparisonOp.ge, 
                  new ConstantValue.DoubleConstant(5), 
                  new ConstantValue.DoubleConstant(3)),
            new TypedValue.ComparisonValue(ComparisonOp.ge, 
                  new ConstantValue.DoubleConstant(5), 
                  new ConstantValue.DoubleConstant(3)));
      Assert.assertEquals(
            new TypedValue.MathOpValue(Op.plus, 
                  new ConstantValue.DoubleConstant(5), 
                  new ConstantValue.DoubleConstant(3)),
            new TypedValue.MathOpValue(Op.plus, 
                  new ConstantValue.DoubleConstant(5), 
                  new ConstantValue.DoubleConstant(3)));
      Assert.assertEquals(
            new TypedValue.GetStaticFieldValue("obj", "field", "I"), 
            new TypedValue.GetStaticFieldValue("obj", "field", "I")); 
      Assert.assertEquals(
            new LambdaFactory(
                  Type.getMethodType(Type.INT_TYPE),
                  new Handle(Opcodes.H_INVOKEVIRTUAL, "obj", "method", "()I"),
                  new ArrayList<>()), 
            new LambdaFactory(
                  Type.getMethodType(Type.INT_TYPE),
                  new Handle(Opcodes.H_INVOKEVIRTUAL, "obj", "method", "()I"),
                  new ArrayList<>())); 
      Assert.assertEquals(
            new MethodCallValue.StaticMethodCallValue(
                  "obj", "method", "()I",
                  new ArrayList<>()), 
            new MethodCallValue.StaticMethodCallValue(
                  "obj", "method", "()I",
                  new ArrayList<>())); 
      Assert.assertEquals(
            new MethodCallValue.VirtualMethodCallValue(
                  "obj", "method", "()I",
                  new ArrayList<>(), 
                  new TypedValue.ThisValue(Type.getObjectType("obj"))), 
            new MethodCallValue.VirtualMethodCallValue(
                  "obj", "method", "()I",
                  new ArrayList<>(),
                  new TypedValue.ThisValue(Type.getObjectType("obj")))); 
      Assert.assertEquals(
            new TypedValue.NewValue("obj"), 
            new TypedValue.NewValue("obj"));
      Assert.assertEquals(
            new TypedValue.CastValue(Type.INT_TYPE, new ConstantValue.FloatConstant(3)), 
            new TypedValue.CastValue(Type.INT_TYPE, new ConstantValue.FloatConstant(3))); 
      Assert.assertEquals(
            new TypedValue.GetFieldValue(
                  "obj", "field", "I", new ConstantValue.NullConstant()), 
            new TypedValue.GetFieldValue(
                  "obj", "field", "I", new ConstantValue.NullConstant()));
      Assert.assertEquals(
            new TypedValue.NotValue(
                  new ConstantValue.BooleanConstant(true)),
            new TypedValue.NotValue(
                  new ConstantValue.BooleanConstant(true)));
      Assert.assertEquals(
            new TypedValue.UnaryMathOpValue(
                  UnaryOp.neg, Type.INT_TYPE,
                  new ConstantValue.IntegerConstant(3)),
            new TypedValue.UnaryMathOpValue(
                  UnaryOp.neg, Type.INT_TYPE,
                  new ConstantValue.IntegerConstant(3)));
   }
   @Test
   public void testNotEquals()
   {
      Assert.assertNotEquals(new TypedValue.ArgValue(Type.INT_TYPE, 2), new TypedValue.ArgValue(Type.INT_TYPE, 1));
      Assert.assertNotEquals(
            new TypedValue.ComparisonValue(ComparisonOp.ge, 
                  new ConstantValue.DoubleConstant(2), 
                  new ConstantValue.DoubleConstant(3)),
            new TypedValue.ComparisonValue(ComparisonOp.ge, 
                  new ConstantValue.DoubleConstant(5), 
                  new ConstantValue.DoubleConstant(3)));
      Assert.assertNotEquals(
            new TypedValue.MathOpValue(Op.plus, 
                  new ConstantValue.DoubleConstant(5), 
                  new ConstantValue.DoubleConstant(3)),
            new TypedValue.MathOpValue(Op.minus, 
                  new ConstantValue.DoubleConstant(5), 
                  new ConstantValue.DoubleConstant(3)));
      Assert.assertNotEquals(
            new TypedValue.GetStaticFieldValue("obj", "field", "I"), 
            new TypedValue.GetStaticFieldValue("obj2", "field", "I")); 
      Assert.assertNotEquals(
            new LambdaFactory(
                  Type.getMethodType(Type.INT_TYPE),
                  new Handle(Opcodes.H_INVOKEVIRTUAL, "obj", "method", "()I"),
                  new ArrayList<>()), 
            new LambdaFactory(
                  Type.getMethodType(Type.INT_TYPE),
                  new Handle(Opcodes.H_INVOKEVIRTUAL, "obj", "method2", "()I"),
                  new ArrayList<>())); 
      Assert.assertNotEquals(
            new MethodCallValue.StaticMethodCallValue(
                  "obj", "method1", "()I",
                  new ArrayList<>()), 
            new MethodCallValue.StaticMethodCallValue(
                  "obj", "method", "()I",
                  new ArrayList<>())); 
      Assert.assertNotEquals(
            new MethodCallValue.VirtualMethodCallValue(
                  "obj", "method", "()I",
                  new ArrayList<>(), 
                  new TypedValue.ThisValue(Type.getObjectType("obj"))), 
            new MethodCallValue.VirtualMethodCallValue(
                  "obj", "method", "()I",
                  new ArrayList<>(),
                  new ConstantValue.NullConstant())); 
      Assert.assertNotEquals(
            new TypedValue.NewValue("obj"), 
            new TypedValue.NewValue("obj1"));
      Assert.assertNotEquals(
            new TypedValue.CastValue(Type.INT_TYPE, new ConstantValue.FloatConstant(3)), 
            new TypedValue.CastValue(Type.INT_TYPE, new ConstantValue.FloatConstant(5))); 
      Assert.assertNotEquals(
            new TypedValue.GetFieldValue(
                  "obj", "field", "I", new ConstantValue.NullConstant()), 
            new TypedValue.GetFieldValue(
                  "obj", "field2", "I", new ConstantValue.NullConstant()));
      Assert.assertNotEquals(
            new TypedValue.NotValue(
                  new ConstantValue.BooleanConstant(true)),
            new TypedValue.NotValue(
                  new ConstantValue.BooleanConstant(false)));
      Assert.assertNotEquals(
            new TypedValue.UnaryMathOpValue(
                  UnaryOp.neg, Type.INT_TYPE,
                  new ConstantValue.IntegerConstant(3)),
            new TypedValue.UnaryMathOpValue(
                  UnaryOp.neg, Type.INT_TYPE,
                  new ConstantValue.IntegerConstant(1)));
   }
   @Test
   public void testNotInvert()
   {
      Assert.assertEquals(
            TypedValue.NotValue.invert(
                  new ConstantValue.BooleanConstant(true)),
            new TypedValue.NotValue(
                  new ConstantValue.BooleanConstant(true)));
      Assert.assertEquals(
            new ConstantValue.BooleanConstant(true),
            TypedValue.NotValue.invert(
                  new TypedValue.NotValue(
                        new ConstantValue.BooleanConstant(true))));
      Assert.assertEquals(
            TypedValue.NotValue.invert(
                  new TypedValue.ComparisonValue(
                        ComparisonOp.le, new ConstantValue.IntegerConstant(1), new ConstantValue.IntegerConstant(2))),
            new TypedValue.ComparisonValue(
                  ComparisonOp.gt, new ConstantValue.IntegerConstant(1), new ConstantValue.IntegerConstant(2)));
   }
}
