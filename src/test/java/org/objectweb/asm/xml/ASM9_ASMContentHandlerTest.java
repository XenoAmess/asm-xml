// ASM: a very small and fast Java bytecode manipulation framework
// Copyright (c) 2000-2011 INRIA, France Telecom
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
// 3. Neither the name of the copyright holders nor the names of its
//    contributors may be used to endorse or promote products derived from
//    this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
// THE POSSIBILITY OF SUCH DAMAGE.
package org.objectweb.asm.xml;

import com.xenoamess.org.objectweb.asm.xml.ASMContentHandler;
import com.xenoamess.org.objectweb.asm.xml.SAXClassAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * ASMContentHandler unit tests
 *
 * @author Eric Bruneton
 */
@Disabled
public class ASM9_ASMContentHandlerTest implements Opcodes {

    ASMContentHandler h;

    ClassVisitor cv;

    MethodVisitor mv;

    @BeforeEach
    public void setUp() throws Exception {
        h =
                new ASMContentHandler(
                        new ClassVisitor(Opcodes.ASM9) {

                            AnnotationVisitor av =
                                    new AnnotationVisitor(Opcodes.ASM9) {

                                        @Override
                                        public AnnotationVisitor visitAnnotation(String name, String desc) {
                                            return this;
                                        }

                                        @Override
                                        public AnnotationVisitor visitArray(String name) {
                                            return this;
                                        }
                                    };

                            @Override
                            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                                return av;
                            }

                            @Override
                            public AnnotationVisitor visitTypeAnnotation(
                                    int typeRef, TypePath typePath, String desc, boolean visible) {
                                return av;
                            }

                            @Override
                            public FieldVisitor visitField(
                                    int access, String name, String desc, String signature, Object value) {
                                return new FieldVisitor(Opcodes.ASM9) {

                                    @Override
                                    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                                        return av;
                                    }

                                    @Override
                                    public AnnotationVisitor visitTypeAnnotation(
                                            int typeRef, TypePath typePath, String desc, boolean visible) {
                                        return av;
                                    }
                                };
                            }

                            @Override
                            public MethodVisitor visitMethod(
                                    int access, String name, String desc, String signature, String[] exceptions) {
                                return new MethodVisitor(Opcodes.ASM9) {

                                    @Override
                                    public AnnotationVisitor visitAnnotationDefault() {
                                        return av;
                                    }

                                    @Override
                                    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                                        return av;
                                    }

                                    @Override
                                    public AnnotationVisitor visitTypeAnnotation(
                                            int typeRef, TypePath typePath, String desc, boolean visible) {
                                        return av;
                                    }

                                    @Override
                                    public AnnotationVisitor visitParameterAnnotation(
                                            int parameter, String desc, boolean visible) {
                                        return av;
                                    }

                                    @Override
                                    public AnnotationVisitor visitInsnAnnotation(
                                            int typeRef, TypePath typePath, String desc, boolean visible) {
                                        return av;
                                    }

                                    @Override
                                    public AnnotationVisitor visitTryCatchAnnotation(
                                            int typeRef, TypePath typePath, String desc, boolean visible) {
                                        return av;
                                    }

                                    @Override
                                    public AnnotationVisitor visitLocalVariableAnnotation(
                                            int typeRef,
                                            TypePath typePath,
                                            Label[] start,
                                            Label[] end,
                                            int[] index,
                                            String desc,
                                            boolean visible) {
                                        return av;
                                    }
                                };
                            }
                        });
        cv = new SAXClassAdapter(ASM9, h, true);
        cv.visit(V1_5, ACC_PUBLIC, "C", null, "java/lang/Object", null);
    }

    protected void methodSetUp() {
        mv = cv.visitMethod(0, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
    }

    @Test
    public void testInvalidOpcode() throws SAXException {
        methodSetUp();
        AttributesImpl attrs = new AttributesImpl();
        assertThrows(SAXException.class, () -> h.startElement("", "opcode", "", attrs));
    }

    @Test
    public void testInvalidValueDescriptor() throws SAXException {
        methodSetUp();
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", "desc", "desc", "", "desc");
        attrs.addAttribute("", "cst", "cst", "", "");
        assertThrows(SAXException.class, () -> h.startElement("", "LDC", "", attrs));
    }

    @Test
    public void testInvalidValue() throws SAXException {
        methodSetUp();
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", "desc", "desc", "", "Ljava/lang/String;");
        attrs.addAttribute("", "cst", "cst", "", "\\");
        assertThrows(SAXException.class, () -> h.startElement("", "LDC", "", attrs));
    }

    @Test
    public void testEndDocument() {
        cv.visitEnd();
        try {
            h.endDocument();
        } catch (SAXException e) {
        }
    }
}
