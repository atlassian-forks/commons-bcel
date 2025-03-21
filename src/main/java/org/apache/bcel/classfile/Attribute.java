/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.bcel.classfile;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.bcel.Const;
import org.apache.bcel.util.Args;

/**
 * Abstract super class for <em>Attribute</em> objects. Currently the <em>ConstantValue</em>, <em>SourceFile</em>,
 * <em>Code</em>, <em>Exceptiontable</em>, <em>LineNumberTable</em>, <em>LocalVariableTable</em>, <em>InnerClasses</em>
 * and <em>Synthetic</em> attributes are supported. The <em>Unknown</em> attribute stands for non-standard-attributes.
 *
 * @see ConstantValue
 * @see SourceFile
 * @see Code
 * @see Unknown
 * @see ExceptionTable
 * @see LineNumberTable
 * @see LocalVariableTable
 * @see InnerClasses
 * @see Synthetic
 * @see Deprecated
 * @see Signature
 */
public abstract class Attribute implements Cloneable, Node {

    private static final boolean debug = Boolean.getBoolean(Attribute.class.getCanonicalName() + ".debug"); // Debugging on/off

    private static final Map<String, Object> READERS = new HashMap<>();

    /**
     * Empty array.
     *
     * @since 6.6.0
     */
    public static final Attribute[] EMPTY_ARRAY = {};

    /**
     * Add an Attribute reader capable of parsing (user-defined) attributes named "name". You should not add readers for the
     * standard attributes such as "LineNumberTable", because those are handled internally.
     *
     * @param name the name of the attribute as stored in the class file
     * @param attributeReader the reader object
     * @deprecated (6.0) Use {@link #addAttributeReader(String, UnknownAttributeReader)} instead
     */
    @java.lang.Deprecated
    public static void addAttributeReader(final String name, final AttributeReader attributeReader) {
        READERS.put(name, attributeReader);
    }

    /**
     * Add an Attribute reader capable of parsing (user-defined) attributes named "name". You should not add readers for the
     * standard attributes such as "LineNumberTable", because those are handled internally.
     *
     * @param name the name of the attribute as stored in the class file
     * @param unknownAttributeReader the reader object
     */
    public static void addAttributeReader(final String name, final UnknownAttributeReader unknownAttributeReader) {
        READERS.put(name, unknownAttributeReader);
    }

    protected static void println(final String msg) {
        if (debug) {
            System.err.println(msg);
        }
    }

    /**
     * Class method reads one attribute from the input data stream. This method must not be accessible from the outside. It
     * is called by the Field and Method constructor methods.
     *
     * @see Field
     * @see Method
     *
     * @param file Input stream
     * @param constantPool Array of constants
     * @return Attribute
     * @throws IOException if an I/O error occurs.
     * @since 6.0
     */
    public static Attribute readAttribute(final DataInput file, final ConstantPool constantPool) throws IOException {
        byte tag = Const.ATTR_UNKNOWN; // Unknown attribute
        // Get class name from constant pool via `name_index' indirection
        final int nameIndex = file.readUnsignedShort();
        final String name = constantPool.getConstantUtf8(nameIndex).getBytes();

        // Length of data in bytes
        final int length = file.readInt();

        // Compare strings to find known attribute
        for (byte i = 0; i < Const.KNOWN_ATTRIBUTES; i++) {
            if (name.equals(Const.getAttributeName(i))) {
                tag = i; // found!
                break;
            }
        }

        // Call proper constructor, depending on `tag'
        switch (tag) {
        case Const.ATTR_UNKNOWN:
            final Object r = READERS.get(name);
            if (r instanceof UnknownAttributeReader) {
                return ((UnknownAttributeReader) r).createAttribute(nameIndex, length, file, constantPool);
            }
            return new Unknown(nameIndex, length, file, constantPool);
        case Const.ATTR_CONSTANT_VALUE:
            return new ConstantValue(nameIndex, length, file, constantPool);
        case Const.ATTR_SOURCE_FILE:
            return new SourceFile(nameIndex, length, file, constantPool);
        case Const.ATTR_CODE:
            return new Code(nameIndex, length, file, constantPool);
        case Const.ATTR_EXCEPTIONS:
            return new ExceptionTable(nameIndex, length, file, constantPool);
        case Const.ATTR_LINE_NUMBER_TABLE:
            return new LineNumberTable(nameIndex, length, file, constantPool);
        case Const.ATTR_LOCAL_VARIABLE_TABLE:
            return new LocalVariableTable(nameIndex, length, file, constantPool);
        case Const.ATTR_INNER_CLASSES:
            return new InnerClasses(nameIndex, length, file, constantPool);
        case Const.ATTR_SYNTHETIC:
            return new Synthetic(nameIndex, length, file, constantPool);
        case Const.ATTR_DEPRECATED:
            return new Deprecated(nameIndex, length, file, constantPool);
        case Const.ATTR_PMG:
            return new PMGClass(nameIndex, length, file, constantPool);
        case Const.ATTR_SIGNATURE:
            return new Signature(nameIndex, length, file, constantPool);
        case Const.ATTR_STACK_MAP:
            // old style stack map: unneeded for JDK5 and below;
            // illegal(?) for JDK6 and above. So just delete with a warning.
            println("Warning: Obsolete StackMap attribute ignored.");
            return new Unknown(nameIndex, length, file, constantPool);
        case Const.ATTR_RUNTIME_VISIBLE_ANNOTATIONS:
            return new RuntimeVisibleAnnotations(nameIndex, length, file, constantPool);
        case Const.ATTR_RUNTIME_INVISIBLE_ANNOTATIONS:
            return new RuntimeInvisibleAnnotations(nameIndex, length, file, constantPool);
        case Const.ATTR_RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS:
            return new RuntimeVisibleParameterAnnotations(nameIndex, length, file, constantPool);
        case Const.ATTR_RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS:
            return new RuntimeInvisibleParameterAnnotations(nameIndex, length, file, constantPool);
        case Const.ATTR_ANNOTATION_DEFAULT:
            return new AnnotationDefault(nameIndex, length, file, constantPool);
        case Const.ATTR_LOCAL_VARIABLE_TYPE_TABLE:
            return new LocalVariableTypeTable(nameIndex, length, file, constantPool);
        case Const.ATTR_ENCLOSING_METHOD:
            return new EnclosingMethod(nameIndex, length, file, constantPool);
        case Const.ATTR_STACK_MAP_TABLE:
            // read new style stack map: StackMapTable. The rest of the code
            // calls this a StackMap for historical reasons.
            return new StackMap(nameIndex, length, file, constantPool);
        case Const.ATTR_BOOTSTRAP_METHODS:
            return new BootstrapMethods(nameIndex, length, file, constantPool);
        case Const.ATTR_METHOD_PARAMETERS:
            return new MethodParameters(nameIndex, length, file, constantPool);
        case Const.ATTR_MODULE:
            return new Module(nameIndex, length, file, constantPool);
        case Const.ATTR_MODULE_PACKAGES:
            return new ModulePackages(nameIndex, length, file, constantPool);
        case Const.ATTR_MODULE_MAIN_CLASS:
            return new ModuleMainClass(nameIndex, length, file, constantPool);
        case Const.ATTR_NEST_HOST:
            return new NestHost(nameIndex, length, file, constantPool);
        case Const.ATTR_NEST_MEMBERS:
            return new NestMembers(nameIndex, length, file, constantPool);
        default:
            // Never reached
            throw new IllegalStateException("Unrecognized attribute type tag parsed: " + tag);
        }
    }

    /**
     * Class method reads one attribute from the input data stream. This method must not be accessible from the outside. It
     * is called by the Field and Method constructor methods.
     *
     * @see Field
     * @see Method
     *
     * @param file Input stream
     * @param constantPool Array of constants
     * @return Attribute
     * @throws IOException if an I/O error occurs.
     */
    public static Attribute readAttribute(final DataInputStream file, final ConstantPool constantPool) throws IOException {
        return readAttribute((DataInput) file, constantPool);
    }

    /**
     * Remove attribute reader
     *
     * @param name the name of the attribute as stored in the class file
     */
    public static void removeAttributeReader(final String name) {
        READERS.remove(name);
    }

    /**
     * @deprecated (since 6.0) will be made private; do not access directly, use getter/setter
     */
    @java.lang.Deprecated
    protected int name_index; // Points to attribute name in constant pool TODO make private (has getter & setter)

    /**
     * @deprecated (since 6.0) (since 6.0) will be made private; do not access directly, use getter/setter
     */
    @java.lang.Deprecated
    protected int length; // Content length of attribute field TODO make private (has getter & setter)

    /**
     * @deprecated (since 6.0) will be made private; do not access directly, use getter/setter
     */
    @java.lang.Deprecated
    protected byte tag; // Tag to distinguish subclasses TODO make private & final; supposed to be immutable

    /**
     * @deprecated (since 6.0) will be made private; do not access directly, use getter/setter
     */
    @java.lang.Deprecated
    protected ConstantPool constant_pool; // TODO make private (has getter & setter)

    protected Attribute(final byte tag, final int nameIndex, final int length, final ConstantPool constantPool) {
        this.tag = tag;
        this.name_index = Args.requireU2(nameIndex, 0, constantPool.getLength(), getClass().getSimpleName() + " name index");
        this.length = length;
        this.constant_pool = constantPool;
    }

    /**
     * Called by objects that are traversing the nodes of the tree implicitly defined by the contents of a Java class.
     * I.e., the hierarchy of methods, fields, attributes, etc. spawns a tree of objects.
     *
     * @param v Visitor object
     */
    @Override
    public abstract void accept(Visitor v);

    /**
     * Use copy() if you want to have a deep copy(), i.e., with all references copied correctly.
     *
     * @return shallow copy of this attribute
     */
    @Override
    public Object clone() {
        Attribute attr = null;
        try {
            attr = (Attribute) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new Error("Clone Not Supported"); // never happens
        }
        return attr;
    }

    /**
     * @return deep copy of this attribute
     */
    public abstract Attribute copy(ConstantPool constantPool);

    /**
     * Dump attribute to file stream in binary format.
     *
     * @param file Output file stream
     * @throws IOException if an I/O error occurs.
     */
    public void dump(final DataOutputStream file) throws IOException {
        file.writeShort(name_index);
        file.writeInt(length);
    }

    /**
     * @return Constant pool used by this object.
     * @see ConstantPool
     */
    public final ConstantPool getConstantPool() {
        return constant_pool;
    }

    /**
     * @return Length of attribute field in bytes.
     */
    public final int getLength() {
        return length;
    }

    /**
     * @return Name of attribute
     * @since 6.0
     */
    public String getName() {
        return constant_pool.getConstantUtf8(name_index).getBytes();
    }

    /**
     * @return Name index in constant pool of attribute name.
     */
    public final int getNameIndex() {
        return name_index;
    }

    /**
     * @return Tag of attribute, i.e., its type. Value may not be altered, thus there is no setTag() method.
     */
    public final byte getTag() {
        return tag;
    }

    /**
     * @param constantPool Constant pool to be used for this object.
     * @see ConstantPool
     */
    public final void setConstantPool(final ConstantPool constantPool) {
        this.constant_pool = constantPool;
    }

    /**
     * @param length length in bytes.
     */
    public final void setLength(final int length) {
        this.length = length;
    }

    /**
     * @param nameIndex of attribute.
     */
    public final void setNameIndex(final int nameIndex) {
        this.name_index = nameIndex;
    }

    /**
     * @return attribute name.
     */
    @Override
    public String toString() {
        return Const.getAttributeName(tag);
    }
}
