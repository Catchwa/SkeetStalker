/*  
 * Copyright 2010-2012 Andrew Brock
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

package org.catchwa.skeetstalker.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.stackwrap4j.StackWrapper;
import net.sf.stackwrap4j.entities.Answer;
import net.sf.stackwrap4j.entities.Question;
import net.sf.stackwrap4j.json.JSONException;
import net.sf.stackwrap4j.query.UnansweredQuery;

import org.catchwa.skeetstalker.client.AnswerTheseService;
import org.catchwa.skeetstalker.server.schema.Stalkee;
import org.catchwa.skeetstalker.server.schema.UnansweredQuestion;
import org.catchwa.skeetstalker.shared.Constants;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class AnswerTheseServiceImpl extends RemoteServiceServlet implements AnswerTheseService
{  
  public String[] getQuestions(int id, String site)
  {    
    Stalkee u = Utils.createUser(id, site);
      if(u == null)
      {
        return new String[]{"User does not exist  "};
      }
    
    ArrayList<String> candidateQuestions = new ArrayList<String>();
    List<String> userTags = u.getTags();
    List<UnansweredQuestion> results;
    
    UnansweredQuery uq = new UnansweredQuery();
    uq.setComments(false);
    uq.setPageSize(50);
    StackWrapper sw = new StackWrapper(site, Constants.API_KEY);
    List<Question> q = null;
    try {
        q = sw.listQuestions(uq);
    } catch (Exception e) {
        e.printStackTrace();
        return new String[0];
    }
    results = convertQuestions(q, site);
           
    for(int i = 0; i < results.size(); i++)
    {
      boolean candidate = false;
      for(int j = 0; j < userTags.size(); j++)
      {
        if(results.get(i).getTags().contains(userTags.get(j)))
        {
          candidate = true;
          break;
        }
      }
      if(candidate)
      {
        List<Integer> answerers = results.get(i).getAnswerers();
        for(int k = 0; k < answerers.size(); k++)
        {
          if(answerers.get(k).intValue() == id)
          {
            candidate = false;
          }
        }
      }
      if(candidate)
      {
        candidateQuestions.add(results.get(i).getTitle()+" "+results.get(i).getId());
      }
    }
    String[] result = new String[candidateQuestions.size()];
    candidateQuestions.toArray(result);
    return result;
  }
  
  private List<UnansweredQuestion> convertQuestions(List<Question> questions, String site)
  {
    List<UnansweredQuestion> uq = new ArrayList<UnansweredQuestion>(questions.size());
    for(int i = 0; i < questions.size(); i++)
    {
      Question q = questions.get(i);
      if(! q.isCommunityOwned())
      {
        try
        {
          uq.add(new UnansweredQuestion(q.getPostId(), site, q.getTitle(), q.getTags(), convertAnswers(questions.get(i).getAnswers())));
        } catch(IOException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch(JSONException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    return uq;
  }
  
  private List<Integer> convertAnswers(List<Answer> answers)
  {
    List<Integer> answerers = new ArrayList<Integer>(answers.size());
    for(int i = 0; i < answers.size(); i++)
    {
      if(answers.get(i).getOwner() != null)
      {
        answerers.add(answers.get(i).getOwner().getId());
      }
    }
    return answerers;
  }
}
