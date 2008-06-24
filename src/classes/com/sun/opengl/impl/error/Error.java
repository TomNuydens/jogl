/*
 * License Applicability. Except to the extent portions of this file are
 * made subject to an alternative license as permitted in the SGI Free
 * Software License B, Version 1.1 (the "License"), the contents of this
 * file are subject only to the provisions of the License. You may not use
 * this file except in compliance with the License. You may obtain a copy
 * of the License at Silicon Graphics, Inc., attn: Legal Services, 1600
 * Amphitheatre Parkway, Mountain View, CA 94043-1351, or at:
 * 
 * http://oss.sgi.com/projects/FreeB
 * 
 * Note that, as provided in the License, the Software is distributed on an
 * "AS IS" basis, with ALL EXPRESS AND IMPLIED WARRANTIES AND CONDITIONS
 * DISCLAIMED, INCLUDING, WITHOUT LIMITATION, ANY IMPLIED WARRANTIES AND
 * CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY, FITNESS FOR A
 * PARTICULAR PURPOSE, AND NON-INFRINGEMENT.
 *
 * NOTE:  The Original Code (as defined below) has been licensed to Sun
 * Microsystems, Inc. ("Sun") under the SGI Free Software License B
 * (Version 1.1), shown above ("SGI License").   Pursuant to Section
 * 3.2(3) of the SGI License, Sun is distributing the Covered Code to
 * you under an alternative license ("Alternative License").  This
 * Alternative License includes all of the provisions of the SGI License
 * except that Section 2.2 and 11 are omitted.  Any differences between
 * the Alternative License and the SGI License are offered solely by Sun
 * and not by SGI.
 * 
 * Original Code. The Original Code is: OpenGL Sample Implementation,
 * Version 1.2.1, released January 26, 2000, developed by Silicon Graphics,
 * Inc. The Original Code is Copyright (c) 1991-2000 Silicon Graphics, Inc.
 * Copyright in any portions created by third parties is as indicated
 * elsewhere herein. All Rights Reserved.
 * 
 * Additional Notice Provisions: The application programming interfaces
 * established by SGI in conjunction with the Original Code are The
 * OpenGL(R) Graphics System: A Specification (Version 1.2.1), released
 * April 1, 1999; The OpenGL(R) Graphics System Utility Library (Version
 * 1.3), released November 4, 1998; and OpenGL(R) Graphics with the X
 * Window System(R) (Version 1.3), released October 19, 1998. This software
 * was created using the OpenGL(R) version 1.2.1 Sample Implementation
 * published by SGI, but has not been independently verified as being
 * compliant with the OpenGL(R) version 1.2.1 Specification.
 */

package com.sun.opengl.impl.glu.error;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

/**
 *
 * @author  Administrator
 */
public class Error {
  
  private static String[] glErrorStrings = {
    "invalid enumerant",
    "invalid value",
    "invalid operation",
    "stack overflow",
    "stack underflow",
    "out of memory",
    "invalid framebuffer operation"
  };
  
  private static String[] gluErrorStrings = {
    "invalid enumerant",
    "invalid value",
    "out of memory",
    "",
    "invalid operation"
  };
  
  /** Creates a new instance of Error */
  public Error() {
  }
  
  public static String gluErrorString( int errorCode ) {
    if( errorCode == 0 ) {
      return( "no error" );
    }
    if( (errorCode >= GL.GL_INVALID_ENUM) && (errorCode <= GL.GL_INVALID_FRAMEBUFFER_OPERATION) ) {
      return( glErrorStrings[ errorCode - GL.GL_INVALID_ENUM ] );
    }
    if( errorCode == 0x8031 /* GL.GL_TABLE_TOO_LARGE */ ) {
      return( "table too large" );
    }
    if( (errorCode >= GLU.GLU_INVALID_ENUM) && (errorCode <= GLU.GLU_INVALID_OPERATION) ) {
      return( gluErrorStrings[ errorCode - GLU.GLU_INVALID_ENUM ] );
    }
//    if( (errorCode >= GLU.GLU_NURBS_ERROR1) && (errorCode <= GLU.GLU_NURBS_ERROR37) ) {
//      return( gluErrorStrings[ errorCode - (GLU.GLU_NURBS_ERROR1 - 1) ] );
//    }
    if( (errorCode >= GLU.GLU_TESS_ERROR1) && (errorCode <= GLU.GLU_TESS_ERROR8) ) {
      return( gluErrorStrings[ errorCode - (GLU.GLU_TESS_ERROR1 - 1) ] );
    }
    return( "error ("+errorCode+")" );
  }
}
