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

/**
 */
public interface Commands {
    String GET = "GET".intern();
    String GETS = "GETS".intern();
    String APPEND = "APPEND".intern();
    String PREPEND = "PREPEND".intern();
    String DELETE = "DELETE".intern();
    String DECR = "DECR".intern();
    String INCR = "INCR".intern();
    String REPLACE = "REPLACE".intern();
    String ADD = "ADD".intern();
    String SET = "SET".intern();
    String CAS = "CAS".intern();
    String STATS = "STATS".intern();
    String VERSION = "VERSION".intern();
    String QUIT = "QUIT".intern();
    String FLUSH_ALL = "FLUSH_ALL".intern();
}
