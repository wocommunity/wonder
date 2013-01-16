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

import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * MINA MessageEncoder responsible for writing a ResponseMessage into the network stream.
 */
public class ResponseEncoder implements MessageEncoder {

    private static final Set<Class<ResponseMessage>> TYPES;

    static {
        Set<Class<ResponseMessage>> types = new HashSet<Class<ResponseMessage>>();
        types.add(ResponseMessage.class);
        TYPES = Collections.unmodifiableSet(types);
    }

    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        ResponseMessage m = (ResponseMessage) message;
        m.out.flip();
        out.write(m.out);
    }

    @SuppressWarnings("unchecked")
    public Set getMessageTypes() {
        return TYPES;
    }
}
