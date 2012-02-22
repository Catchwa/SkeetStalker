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

package org.catchwa.skeetstalker.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

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
  @SuppressWarnings("unchecked")
  public String[] getQuestions(int id, String site)
  {    
    Stalkee u = Utils.getCachedUser(id, site);
    
    if(u != null && System.currentTimeMillis() - u.getLastUpdated() > org.catchwa.skeetstalker.shared.Constants.SERVER_USER_TAG_REFRESH_INTERVAL)
    {
      Utils.removeUser(id, site);
      u = null;
    }

    PersistenceManager pm = PMF.get().getPersistenceManager();
    if(u == null)
    {
      u = Utils.createUser(id, site);
      if(u == null)
      {
        return new String[]{"User does not exist  "};
      }
    }
    
    ArrayList<String> candidateQuestions = new ArrayList<String>();
    List<String> userTags = u.getTags();
    List<UnansweredQuestion> results;
    
    Query query = pm.newQuery();
    query = pm.newQuery(UnansweredQuestion.class);
    query.setFilter("site == targetSite");
    query.setOrdering("created desc");
    query.declareParameters("String targetSite");
    
    Query innerQuery = null;
    try
    {
      results = (List<UnansweredQuestion>) query.execute(site);
      if(results.size() > 0 && (System.currentTimeMillis() - results.get(0).getCreated()) < org.catchwa.skeetstalker.shared.Constants.SERVER_UNANSWERED_QUESTION_REFRESH_INTERVAL)
      {
        // Use cached version
      }
      else
      { 
        try
        {
          UnansweredQuery uq = new UnansweredQuery();
          uq.setComments(false);
          uq.setPageSize(50);
          StackWrapper sw = new StackWrapper(site, Constants.API_KEY);
          List<Question> q = sw.listQuestions(uq);
          results = convertQuestions(q, site);
          innerQuery = pm.newQuery(UnansweredQuestion.class);
          innerQuery.setFilter("site == targetSite");
          innerQuery.declareParameters("String targetSite");
          innerQuery.deletePersistentAll(site);
          for(int i = 0; i < results.size(); i++)
          {
            pm.makePersistent(results.get(i));
          }
        } catch(Exception e)
        {
          e.printStackTrace();
          pm.close();
          return new String[0];
        }
        finally
        {
          if(innerQuery != null)
          {
            innerQuery.closeAll();
          }
        }
      }
    }
    finally
    {
      query.closeAll();
    }
    
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
