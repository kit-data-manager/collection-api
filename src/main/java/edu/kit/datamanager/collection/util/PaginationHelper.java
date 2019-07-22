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

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 *
 * @author jejkal
 */
public class PaginationHelper{

  private int currentPage = 0;
  private int elementsPerPage = 20;
  private long totalElements = 0;

  public static PaginationHelper create(int currentPage, long totalElements){
    PaginationHelper helper = new PaginationHelper();
    helper.currentPage = currentPage;
    helper.totalElements = totalElements;
    return helper;
  }

  public PaginationHelper withElementsPerPage(int elementsPerPage){
    this.elementsPerPage = elementsPerPage;
    return this;
  }

  public String getNextPageLink(){
    int nextPage = currentPage + 1;
    long totalPages = getPageCount();
    return (nextPage >= totalPages) ? null : ServletUriComponentsBuilder.fromCurrentRequest().replaceQueryParam("page", nextPage).replaceQueryParam("size", elementsPerPage).build().toString();
  }

  public String getPrevPageLink(){
    int prevPage = currentPage - 1;
    return (prevPage < 0) ? null : ServletUriComponentsBuilder.fromCurrentRequest().replaceQueryParam("page", prevPage).replaceQueryParam("size", elementsPerPage).build().toString();
  }

  public int getPageCount(){
    return (totalElements > 0) ? (int) Math.rint(totalElements / elementsPerPage) + ((totalElements % elementsPerPage != 0) ? 1 : 0) : 0;
  }
}
