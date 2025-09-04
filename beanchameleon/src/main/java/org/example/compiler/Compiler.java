package org.example.compiler;

import java.io.IOException;
import java.util.Map;

public interface Compiler {
    Map<String, byte[]> compile(String javaSrc) throws IOException;
}
