/*
 * Copyright (c) 2003 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright (c) 2010 JogAmp Community. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
 * MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
 * ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
 * DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
 * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 * 
 * Sun gratefully acknowledges that this software was originally authored
 * and developed by Kenneth Bradley Russell and Christopher John Kline.
 */

package jogamp.opengl;

import javax.media.opengl.*;

import java.util.*;

/**
 * A utility object intended to be used by implementations to act as a cache
 * of which OpenGL extensions are currently available on both the host machine
 * and display.
 */
final class ExtensionAvailabilityCache {
  protected static final boolean DEBUG = GLContextImpl.DEBUG;

  ExtensionAvailabilityCache(GLContextImpl context)
  {
    this.context = context;
  }

  /**
   * Flush the cache. The cache will be rebuilt lazily as calls to {@link
   * #isExtensionAvailable(String)} are received.
   */
  final void flush()
  {
    if(DEBUG) {
        System.out.println("ExtensionAvailabilityCache: Flush availability OpenGL "+context.getGLVersion());
    }
    initialized = false;
    glExtensions = null;
    glExtensionCount = 0;
    glXExtensions = null;
    glXExtensionCount = 0;
    availableExtensionCache.clear();
  }

  /**
   * Flush the cache and rebuild the cache.
   */
  final void reset() {
    flush();
    initAvailableExtensions();
  }

  final boolean isInitialized() {
    return initialized && !availableExtensionCache.isEmpty() ;
  }

  final int getTotalExtensionCount() {
    initAvailableExtensions();
    return availableExtensionCache.size();
  }
  
  final boolean isExtensionAvailable(String glExtensionName) {
    initAvailableExtensions();
    return availableExtensionCache.contains(mapGLExtensionName(glExtensionName));
  }

  final int getPlatformExtensionCount() {
    initAvailableExtensions();
    return glXExtensionCount;
  }
  
  final String getPlatformExtensionsString() {
    initAvailableExtensions();
    return glXExtensions;
  }

  final int getGLExtensionCount() {
    initAvailableExtensions();
    return glExtensionCount;
  }
  
  final String getGLExtensionsString() {
    initAvailableExtensions();
    if(DEBUG) {
        System.err.println("ExtensionAvailabilityCache: getGLExtensions() called");
    }
    return glExtensions;
  }

  private final void initAvailableExtensions() {
    GL gl = context.getGL();
    // if hash is empty (meaning it was flushed), pre-cache it with the list
    // of extensions that are in the GL_EXTENSIONS string
    if (availableExtensionCache.isEmpty() || !initialized) {
      if (DEBUG) {
         System.err.println(getThreadName() + ":ExtensionAvailabilityCache: Pre-caching init "+gl+", OpenGL "+context.getGLVersion());
      }

      boolean useGetStringi = false;

      // Use 'glGetStringi' only for ARB GL3 context,
      // on GL2 platforms the function might be available, but not working.
      if ( context.isGL3() ) {
          if ( ! context.isFunctionAvailable("glGetStringi") ) {
            if(DEBUG) {
                System.err.println("GLContext: GL >= 3.1 usage, but no glGetStringi");
            }
          } else {
              useGetStringi = true;
          }
      }

      if (DEBUG) {
        System.err.println(getThreadName() + ":ExtensionAvailabilityCache: Pre-caching extension availability OpenGL "+context.getGLVersion()+
                           ", use "+ ( useGetStringi ? "glGetStringi" : "glGetString" ) );
      }

      HashSet<String> glExtensionSet = new HashSet<String>(gl.isGLES() ? 50 : 320); // far less gl extension expected on mobile 
      if(useGetStringi) {
        GL2GL3 gl2gl3 = gl.getGL2GL3();
        final int count;
        {
            int[] val = { 0 } ;
            gl2gl3.glGetIntegerv(GL2GL3.GL_NUM_EXTENSIONS, val, 0);
            count = val[0];
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if(i > 0) {
                sb.append(" ");                
            }
            final String ext = gl2gl3.glGetStringi(GL.GL_EXTENSIONS, i);
            glExtensionSet.add(ext);
            sb.append(ext);
        }
        if(0==count || sb.length()==0) {
            // fall back ..
            useGetStringi=false;
        } else {
            glExtensions = sb.toString();
        }
      }
      if(!useGetStringi) {
          glExtensions = gl.glGetString(GL.GL_EXTENSIONS);
          if(null != glExtensions) {
              StringTokenizer tok = new StringTokenizer(glExtensions);
              while (tok.hasMoreTokens()) {
                  glExtensionSet.add(tok.nextToken().trim());
              }
          }
      }
      glExtensionCount = glExtensionSet.size(); 
      if (DEBUG) {
          System.err.println(getThreadName() + ":ExtensionAvailabilityCache: GL_EXTENSIONS: "+glExtensionCount);
      }
      
      // Platform Extensions
      HashSet<String> glXExtensionSet = new HashSet<String>(50);
      {         
          // unify platform extension .. might have duplicates          
          StringTokenizer tok = new StringTokenizer(context.getPlatformExtensionsStringImpl().toString());
          while (tok.hasMoreTokens()) {
              glXExtensionSet.add(tok.nextToken().trim());              
          }
          final StringBuilder sb = new StringBuilder();
          for(Iterator<String> iter = glXExtensionSet.iterator(); iter.hasNext(); ) {
              sb.append(iter.next());
              if(iter.hasNext()) {
                  sb.append(" ");                
              }
          }
          glXExtensions = sb.toString();
          glXExtensionCount = glXExtensionSet.size();
      }
      
      availableExtensionCache.addAll(glExtensionSet);
      availableExtensionCache.addAll(glXExtensionSet);
      
      if (DEBUG) {
          System.err.println(getThreadName() + ":ExtensionAvailabilityCache: GLX_EXTENSIONS: "+glXExtensionCount);
          System.err.println(getThreadName() + ":ExtensionAvailabilityCache: GL vendor: " + gl.glGetString(GL.GL_VENDOR));
          System.err.println(getThreadName() + ":ExtensionAvailabilityCache: ALL EXTENSIONS: "+availableExtensionCache.size());
      }

      if(!context.isGLES()) {
          int major[] = new int[] { context.getGLVersionMajor() };
          int minor[] = new int[] { context.getGLVersionMinor() };
          while (GLContext.isValidGLVersion(major[0], minor[0])) {
            availableExtensionCache.add("GL_VERSION_" + major[0] + "_" + minor[0]);
            if (DEBUG) {
                System.err.println(getThreadName() + ":ExtensionAvailabilityCache: Added GL_VERSION_" + major[0] + "_" + minor[0] + " to known extensions");
            }
            if(!GLContext.decrementGLVersion(major, minor)) break;
          }
      }

      // put a dummy var in here so that the cache is no longer empty even if
      // no extensions are in the GL_EXTENSIONS string
      availableExtensionCache.add("<INTERNAL_DUMMY_PLACEHOLDER>");

      initialized = true;
    }
  }

  // FIXME: hack to re-enable GL_NV_vertex_array_range extension after
  // recent upgrade to new wglext.h and glxext.h headers
  private static String mapGLExtensionName(String extensionName) {
    if (extensionName != null &&
        (extensionName.equals("WGL_NV_vertex_array_range") ||
         extensionName.equals("GLX_NV_vertex_array_range"))) 
      return "GL_NV_vertex_array_range";
    return extensionName;
  }

  //----------------------------------------------------------------------
  // Internals only below this point
  //

  private boolean initialized = false;
  private String glExtensions = null;
  private int glExtensionCount = 0;
  private String glXExtensions = null;
  private int glXExtensionCount = 0;
  private HashSet<String> availableExtensionCache = new HashSet<String>(50);
  private GLContextImpl context;

  static String getThreadName() {
    return Thread.currentThread().getName();
  }

}
