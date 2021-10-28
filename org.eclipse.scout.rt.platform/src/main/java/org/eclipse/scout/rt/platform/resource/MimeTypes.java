/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.resource;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.util.FileUtility;

/**
 * Support for extensible {@link MimeType} enums
 */
public final class MimeTypes {
  private static final Map<String/*ext-lowercase*/, IMimeType> EXT_TO_MIMETYPE = Collections.synchronizedMap(new LinkedHashMap<>());
  private static final Pattern EXT_LIST_PATTERN = Pattern.compile("[\\s,;.]");

  static {
    for (MimeType t : MimeType.values()) {
      register(t, false);
    }
  }

  private MimeTypes() {
  }

  /**
   * @return true if the {@link IMimeType} was added
   */
  public static boolean register(IMimeType t, boolean overwrite) {
    if (t.getFileExtension() == null) {
      return false;
    }
    if (overwrite) {
      EXT_TO_MIMETYPE.put(t.getFileExtension(), t);
      return true;
    }
    else {
      return EXT_TO_MIMETYPE.putIfAbsent(t.getFileExtension(), t) != null;
    }
  }

  /**
   * @return the {@link IMimeType} that was removed or null
   */
  public static IMimeType unregister(String fileExtension) {
    if (fileExtension == null) {
      return null;
    }
    return EXT_TO_MIMETYPE.remove(fileExtension);
  }

  public static IMimeType findByFileExtension(String fileExtension) {
    if (fileExtension == null) {
      return null;
    }
    return EXT_TO_MIMETYPE.get(fileExtension.toLowerCase(Locale.ROOT));
  }

  public static Collection<IMimeType> findByMimeTypeName(String mimeTypeName) {
    if (mimeTypeName == null) {
      return Collections.emptyList();
    }
    return EXT_TO_MIMETYPE
        .values()
        .stream()
        .filter(t -> Objects.equals(mimeTypeName, t.getType()))
        .collect(Collectors.toList());
  }

  public static Collection<IMimeType> findByContentMagic(BinaryResource res) {
    if (res == null) {
      return Collections.emptyList();
    }
    return EXT_TO_MIMETYPE
        .values()
        .stream()
        .filter(t -> t.getMagic() != null && t.getMagic().matches(res))
        .collect(Collectors.toList());
  }

  /**
   * Verify file content or {@link BinaryResource}. Check headers and content in order to find out if the file is valid
   * or corrupt or malware
   * <p>
   * If no {@link IMimeMagic} is available for resource then nothing is done.
   *
   * @return true if verification was successful
   */
  public static boolean verifyMagic(BinaryResource res) {
    String ext = FileUtility.getFileExtension(res.getFilename());
    IMimeType mimeType = MimeTypes.findByFileExtension(ext);
    if (mimeType != null && mimeType.getMagic() != null) {
      //there is a verifier, let's decide
      if (mimeType.getMagic().matches(res)) {
        return true;
      }
      return false;
    }
    //no verifier, check if the file content yields a different mime than the file extension
    Set<String> detectedExt = MimeTypes.findByContentMagic(res).stream().map(t -> t.getFileExtension()).collect(Collectors.toSet());
    if (detectedExt.isEmpty() || detectedExt.contains(ext)) {
      return true;
    }
    return false;
  }
}
