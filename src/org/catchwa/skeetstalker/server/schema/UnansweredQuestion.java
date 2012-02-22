/*  
 * Copyright 2010 Andrew Brock
 * 
 * This file is part of SkeetStalker.
 *
 * SkeetStalker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SkeetStalker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SkeetStalker.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.catchwa.skeetstalker.server.schema;

import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class UnansweredQuestion
{
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private String key;
  
  @Persistent
  private long id;
  
  @Persistent
  private String site;
  
  @Persistent
  private String title;
  
  @Persistent
  private List<String> tags;
  
  @Persistent
  private List<Integer> answerers;
  
  @Persistent
  private long created;
    
  public UnansweredQuestion(long id, String site, String title, List<String> tags, List<Integer> list)
  {
    this.id = id;
    this.site = site;
    this.title = title;
    this.tags = tags;
    this.answerers = list;
    this.created = System.currentTimeMillis();
    key = id+"@"+site;
  }
  
  public String getKey()
  {
    return key;
  }
  
  public long getId()
  {
    return id;
  }
  
  public String getSite()
  {
    return site;
  }
  
  public String getTitle()
  {
    return title;
  }
  
  public List<String> getTags()
  {
    return tags;
  }
  
  public List<Integer> getAnswerers()
  {
    return answerers;
  }
  
  public long getCreated()
  {
    return created;
  }
}
