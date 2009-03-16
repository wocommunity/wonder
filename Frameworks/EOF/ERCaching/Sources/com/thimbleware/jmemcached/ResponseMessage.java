/**
 *  Copyright 2008 ThimbleWare Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.thimbleware.jmemcached;

import org.apache.mina.common.ByteBuffer;

import java.io.Serializable;

/**
 * Represents an outbound response on the network stream.
 */
public class ResponseMessage implements Serializable {
    public ByteBuffer out;

    public ResponseMessage() {
        out = ByteBuffer.allocate(1024);
        out.setAutoExpand(true);
    }
}
