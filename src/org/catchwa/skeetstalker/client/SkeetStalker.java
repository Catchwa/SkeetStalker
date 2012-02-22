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


package org.catchwa.skeetstalker.client;

import java.util.HashMap;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

public class SkeetStalker implements EntryPoint
{
  private LastOnlineServiceAsync lastOnlineService = GWT.create(LastOnlineService.class);
  private AnswerTheseServiceAsync answerTheseService = GWT.create(AnswerTheseService.class);
  private FindUserServiceAsync findUserService = GWT.create(FindUserService.class);
  private StackSitesServiceAsync stackSitesService = GWT.create(StackSitesService.class);
  private static ListBox siteChoices = new ListBox();
  private static TextBox idTextBox = new TextBox();
  private static TextBox userInput = new TextBox();
  private static String site = org.catchwa.skeetstalker.shared.Constants.DEFAULT_SITE;
  private static int id = org.catchwa.skeetstalker.shared.Constants.DEFAULT_ID;
  private static DisclosurePanel dp = new DisclosurePanel("Want to stalk someone else?");
  private static DialogBox db = new DialogBox();
  private static Grid dialogGrid = new Grid(2, 2);
  private static HashMap<String, String> sites;
  
  /**
   * This is the entry point method.
   */
  public void onModuleLoad()
  {
    final Grid table = new Grid(1,1);
    final HTML lastOnlineLabel = new HTML();
    table.setHTML(0, 0, "<b>Questions they'll probably answer (unless you get there first!)</b>");
    
    RootPanel.get("lastOnlineContainer").add(lastOnlineLabel);
    RootPanel.get("questionsContainer").add(table);
    
    Grid g = new Grid(2, 3);
    g.setWidget(0, 0, new Label("User ID"));
    idTextBox.setText(""+org.catchwa.skeetstalker.shared.Constants.DEFAULT_ID);
    g.setWidget(0, 1, idTextBox);
    
    siteChoices.setVisibleItemCount(1);
    sites = new HashMap<String, String>();
    sites.put("Stack Overflow", "http://api.stackoverflow.com");
    siteChoices.addItem("Stack Overflow");
    populateSitesDropdown();
    g.setWidget(0, 2, siteChoices);
    
    Button stalkButton = new Button();
    stalkButton.setText("Stalk!");
    stalkButton.addClickHandler(new ClickHandler(){
      @Override
      public void onClick(ClickEvent event)
      {
        site = sites.get(siteChoices.getItemText(siteChoices.getSelectedIndex()));
        id = Integer.parseInt(idTextBox.getText());
        resetTable();
        refreshLastOnline();
        refreshAnswerThese();
        dp.setOpen(false);
      }      
    });
    g.setWidget(1, 0, stalkButton);
    
    Button userIdButton = new Button();
    userIdButton.setText("Find User ID...");
    userIdButton.addClickHandler(new ClickHandler(){
      @Override
      public void onClick(ClickEvent event)
      {
        createDialogBox();
        db.show();
      }
    });
    g.setWidget(1, 1, userIdButton);
    
    dp.add(g);
    RootPanel.get().add(dp);
    
    refreshLastOnline();
    refreshAnswerThese();
    
    Timer t1 = new Timer(){
      @Override
      public void run()
      {
        refreshAnswerThese();
      }
    };
    
    Timer t2 = new Timer(){
      @Override
      public void run()
      {
        refreshLastOnline();
      }
    };
    
    t1.scheduleRepeating(org.catchwa.skeetstalker.shared.Constants.CLIENT_QUESTIONS_TABLE_REFRESH_INTERVAL);
    t2.scheduleRepeating(org.catchwa.skeetstalker.shared.Constants.CLIENT_LAST_ONLINE_REFRESH_INTERVAL);
  }
  
  private void populateSitesDropdown()
  {
    if (stackSitesService == null) {
      stackSitesService = GWT.create(StackSitesService.class);
    }
    AsyncCallback<String[]> callback = new AsyncCallback<String[]>() {
      public void onFailure(Throwable caught) {
        // TODO: Do something with errors.
      }
      
      public void onSuccess(String[] result)
      {
        while(siteChoices.getItemCount() > 0)
        {
          siteChoices.removeItem(0);
        }
        sites.clear();
        for(int i = 0; i < result.length; i++)
        {
          String[] splat = result[i].split("!");
          sites.put(splat[1], splat[0]);
          siteChoices.addItem(splat[1]);
        }
      }
    };
    stackSitesService.getSites(callback);
  }
  
  private void createDialogBox()
  {
    db = new DialogBox();
    db.setGlassEnabled(true);
    db.setAnimationEnabled(true);
    db.setText("Find user on "+siteChoices.getItemText(siteChoices.getSelectedIndex()));
    db.setTitle("Find user on "+siteChoices.getItemText(siteChoices.getSelectedIndex()));
    
    dialogGrid = new Grid(2, 2);
    dialogGrid.setWidget(0, 0, new Label("Username: "));
    dialogGrid.setWidget(0, 1, userInput);
    
    Button findUser = new Button("Find...");
    findUser.addClickHandler(new ClickHandler(){
      @Override
      public void onClick(ClickEvent event)
      {
        findUser(userInput.getText(), sites.get(siteChoices.getItemText(siteChoices.getSelectedIndex())));
      }
    });
    dialogGrid.setWidget(1, 1, findUser);
    db.add(dialogGrid);
    
    Button closeDialog = new Button("Close");
    closeDialog.addClickHandler(new ClickHandler(){
      @Override
      public void onClick(ClickEvent event)
      {
        db.hide();
      }
    });
    dialogGrid.setWidget(1, 0, closeDialog);
  }
  
  private void findUser(String name, String site)
  {
    if (findUserService == null) {
      findUserService = GWT.create(FindUserService.class);
    }
    AsyncCallback<String[]> callback = new AsyncCallback<String[]>() {
      public void onFailure(Throwable caught) {
        // TODO: Do something with errors.
      }
      
      public void onSuccess(String[] result)
      {
        dialogGrid.resizeRows(2 + result.length);
        for(int i = 0; i < result.length; i++)
        {
          int splitHere = result[i].lastIndexOf(' ');
          String name = result[i].substring(0, splitHere);
          final String id = result[i].substring(splitHere + 1, result[i].length());
          String base = sites.get(siteChoices.getItemText(siteChoices.getSelectedIndex()));
          base = base.replace("api.", "");
          dialogGrid.setHTML(2 + i, 0, "<a href=\""+base+"/users/"+id+"\" target=\"_blank\">"+name+"</a>");
          Button b = new Button("Select");
          b.addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event)
            {
              idTextBox.setText(id);
              db.hide();
            }
          });
          dialogGrid.setWidget(2 + i, 1, b);
        }
      }
    };
    findUserService.getUsers(name, site, callback);
  }
  
  private void refreshLastOnline()
  {
    if (lastOnlineService == null) {
      lastOnlineService = GWT.create(LastOnlineService.class);
    }
    AsyncCallback<String> callback = new AsyncCallback<String>() {
      public void onFailure(Throwable caught) {
        // TODO: Do something with errors.
      }

      public void onSuccess(String result) {
        HTML label = (HTML) RootPanel.get("lastOnlineContainer").getWidget(0);
        label.setHTML(result);
      }
    };
    lastOnlineService.getLastOnline(id, site, callback);
  }
  
  private void refreshAnswerThese()
  {
    if (answerTheseService == null) {
      answerTheseService = GWT.create(AnswerTheseService.class);
    }
    AsyncCallback<String[]> callback = new AsyncCallback<String[]>() {
      public void onFailure(Throwable caught) {
        // TODO: Do something with errors.
      }

      public void onSuccess(String[] result) {
        Grid table = (Grid) RootPanel.get("questionsContainer").getWidget(0);
        for(int i = 0; i < result.length; i++)
        {
          int splitHere = result[i].lastIndexOf(' ');
          String title = result[i].substring(0, splitHere);
          String id = result[i].substring(splitHere + 1, result[i].length());
          String base = sites.get(siteChoices.getItemText(siteChoices.getSelectedIndex()));
          base = base.replace("api.", "");
          String html = "<a href=\""+base+"/questions/"+id+"\" target=\"_blank\">"+title+"</a>";
          if(! containsRow(table, html))
          {
            if(table.getRowCount() > 2)
            {
              table.insertRow(1);
              table.setHTML(1, 0, html);
              while(table.getRowCount() > org.catchwa.skeetstalker.shared.Constants.CLIENT_TABLE_ROW_LIMIT)
              {
                table.removeRow(table.getRowCount() - 1);
              }
            }
            else
            {
              table.resizeRows(3);
              table.setHTML(2, 0, table.getHTML(1, 0));
              table.setHTML(1, 0, html);
            }
          }
        }
      }
    };
    answerTheseService.getQuestions(id, site, callback);
  }
  
  private boolean containsRow(Grid table, String html)
  {
    for(int i = 0; i < table.getRowCount(); i++)
    {
      if(table.getHTML(i, 0).toString().equals(html))
      {
        return true;
      }
    }
    return false;
  }
  
  private void resetTable()
  {
    Grid table = (Grid) RootPanel.get("questionsContainer").getWidget(0);
    while(table.getRowCount() > 0)
    {
      table.removeRow(0);
    }
    table.resizeRows(1);
    table.setHTML(0, 0, "<b>Questions they'll probably answer (unless you get there first!)</b>");
  }
}
