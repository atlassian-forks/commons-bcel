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
 *
 */
package org.apache.bcel.verifier.structurals;

import java.io.Serializable;

/**
 * A placeholder class that can be used to create an ObjectType of which has some of the properties arrays have. They
 * implement java.lang.Cloneable and java.io.Serializable and they extend java.lang.Object.
 *
 */
public class GenericArray implements Cloneable, Serializable {

    private static final long serialVersionUID = 1991183963515237894L;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
