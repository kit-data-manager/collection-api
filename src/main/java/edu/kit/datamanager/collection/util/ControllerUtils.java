/*
 * Copyright 2019 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.datamanager.collection.util;

import edu.kit.datamanager.collection.domain.EtagSupport;
import edu.kit.datamanager.exceptions.EtagMismatchException;
import edu.kit.datamanager.exceptions.EtagMissingException;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class ControllerUtils{

  private static final Logger LOG = LoggerFactory.getLogger(ControllerUtils.class);

  /**
   * Check the ETag provided by the caller against the current ETag provided by
   * a resource. If both ETags are not matching, an EtagMismatchException is
   * thrown.
   *
   * @param request The WebRequest containing all headers, e.g. the ETag.
   * @param resource A resource capable of providing its own ETag.
   *
   * @throws EtagMismatchException if the provided ETag is not matching the
   * current ETag.
   */
  public static void checkEtag(HttpServletRequest request, EtagSupport resource) throws EtagMismatchException{
    String etag = resource.getEtag();
    LOG.trace("Checking ETag for resource with ETag {}.", etag);
    String etagValue = request.getHeader("If-Match");
    LOG.trace("Received ETag: {}", etagValue);

    if(etagValue == null){
      throw new EtagMissingException("If-Match header with valid etag is missing.");
    }

    if(!etagValue.equals(etag)){
      throw new EtagMismatchException("ETag not matching or not provided.");
    }
  }
}
