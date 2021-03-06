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
package edu.kit.datamanager.collection.domain.d3;

import edu.kit.datamanager.collection.domain.CollectionItemMappingMetadata;
import lombok.Data;

/**
 *
 * @author jejkal
 */
@Data
public class MemberItemNode extends Node{

  private String location;
  private String dataType;
  private String ontology;
  private CollectionItemMappingMetadata mapping;

  @Override
  public TYPE getType(){
    return Node.TYPE.MEMBER_ITEM;
  }

}
