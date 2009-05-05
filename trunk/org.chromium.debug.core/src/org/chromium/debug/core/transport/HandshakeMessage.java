// Copyright (c) 2009 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.debug.core.transport;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collections;

/**
 * This handshake message should be sent over the wire first, to establish a
 * debugger connection with a Chromium instance.
 */
public class HandshakeMessage extends Message {

  public HandshakeMessage() {
    super(Collections.<String, String> emptyMap(), null);
  }

  @Override
  public void sendThrough(BufferedWriter writer) throws IOException {
    writer.write("ChromeDevToolsHandshake\r\n"); //$NON-NLS-1$
    writer.flush();
  }

  @Override
  public String toString() {
    return "[ChromeDevToolsHandshake request message]"; //$NON-NLS-1$
  }
}