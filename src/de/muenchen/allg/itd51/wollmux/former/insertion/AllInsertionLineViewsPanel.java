/*
* Dateiname: AllInsertionLineViewsPanel.java
* Projekt  : WollMux
* Funktion : Enth�lt alle OneInsertionLineViews.
* 
* Copyright: Landeshauptstadt M�nchen
*
* �nderungshistorie:
* Datum      | Wer | �nderungsgrund
* -------------------------------------------------------------------
* 13.09.2006 | BNK | Erstellung
* -------------------------------------------------------------------
*
* @author Matthias Benkmann (D-III-ITD 5.1)
* @version 1.0
* 
*/
package de.muenchen.allg.itd51.wollmux.former.insertion;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import de.muenchen.allg.itd51.wollmux.former.BroadcastListener;
import de.muenchen.allg.itd51.wollmux.former.BroadcastObjectSelection;
import de.muenchen.allg.itd51.wollmux.former.FormularMax4000;
import de.muenchen.allg.itd51.wollmux.former.IndexList;
import de.muenchen.allg.itd51.wollmux.former.view.View;
import de.muenchen.allg.itd51.wollmux.former.view.ViewChangeListener;


/**
 * Enth�lt alle OneInsertionLineViews.
 *
 * @author Matthias Benkmann (D-III-ITD 5.1)
 */
public class AllInsertionLineViewsPanel implements View
{
  /**
   * Rand um Buttons (in Pixeln).
   */
  private final static int BUTTON_BORDER = 2;

  /**
   * Der {@link FormularMax4000} zu dem diese View geh�rt.
   */
  private FormularMax4000 formularMax4000;
  
  /**
   * Wird auf alle {@link OneInsertionLineView}s registriert.
   */
  private ViewChangeListener myViewChangeListener;
  
  /**
   * Das JPanel, das die ganze View enth�lt.
   */
  private JPanel myPanel;
  
  /**
   * Das JPanel, das die {@link de.muenchen.allg.itd51.wollmux.former.insertion.OneInsertionLineView}s
   * enth�lt.
   */
  private JPanel mainPanel;
  
  /**
   * Die JScrollPane, die {@link #mainPanel} enth�lt.
   */
  private JScrollPane scrollPane;
  
  /**
   * Die Liste der {@link OneInsertionLineView}s in dieser View.
   */
  private List views = new Vector();
  
  /**
   * Liste von Indizes der selektierten Objekte in der {@link #views} Liste. 
   */
  private IndexList selection = new IndexList();
  
  /**
   * Erzeugt ein AllInsertionLineViewsPanel, die den Inhalt von
   * insertionModelList anzeigt. ACHTUNG! insertionModelList sollte leer sein,
   * da nur neu hinzugekommene Elemente in der View angezeigt werden.
   * @author Matthias Benkmann (D-III-ITD 5.1)
   * TESTED
   */
  public AllInsertionLineViewsPanel(InsertionModelList insertionModelList, FormularMax4000 formularMax4000)
  {
    this.formularMax4000 = formularMax4000;
    insertionModelList.addListener(new MyItemListener());
    formularMax4000.addBroadcastListener(new MyBroadcastListener());
    myViewChangeListener = new MyViewChangeListener();
  
    mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

    mainPanel.add(Box.createGlue());
    
    scrollPane = new JScrollPane(mainPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    
    JPanel buttonPanel = new JPanel(new GridBagLayout());
    
    myPanel = new JPanel(new BorderLayout());
    myPanel.add(scrollPane, BorderLayout.CENTER);
    myPanel.add(buttonPanel, BorderLayout.SOUTH);
    
    GridBagConstraints gbcButton = new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,       new Insets(BUTTON_BORDER,BUTTON_BORDER,BUTTON_BORDER,BUTTON_BORDER),0,0);
    JButton button = new JButton("L�schen");
    button.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e)
      {
        
      }});
    buttonPanel.add(button, gbcButton);
    
    ++gbcButton.gridx;
    button = new JButton("Button 2");
    button.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e)
      {
    
      }});
    buttonPanel.add(button, gbcButton);
    
    ++gbcButton.gridx;
  
  }
  
  /**
   * F�gt dieser View eine {@link OneInsertionLineView} f�r model hinzu.
   * @author Matthias Benkmann (D-III-ITD 5.1)
   * TESTED
   */
  private void addItem(InsertionModel model)
  {
    OneInsertionLineView view = new OneInsertionLineView(model, myViewChangeListener, formularMax4000);
    views.add(view);
    
    /*
     * view vor dem letzten Element von mainPanel einf�gen, weil das letzte
     * Element immer ein Glue sein soll.
     */
    mainPanel.add(view.JComponent(), mainPanel.getComponentCount() - 1);
    
    mainPanel.validate();
    scrollPane.validate();
  }
  
  /**
   * Entfernt view aus diesem Container (falls dort vorhanden).
   * @author Matthias Benkmann (D-III-ITD 5.1)
   * TESTED
   */
  private void removeItem(OneInsertionLineView view)
  {
    int index = views.indexOf(view);
    if (index < 0) return;
    views.remove(index);
    mainPanel.remove(view.JComponent());
    mainPanel.validate();
    selection.remove(index); 
    selection.fixup(index, -1, views.size() - 1);
  }
  
  /**
   * Hebt die Selektion aller Elemente auf.
   * 
   * @author Matthias Benkmann (D-III-ITD 5.1)
   * TESTED */
  private void clearSelection()
  {
    Iterator iter = selection.iterator();
    while (iter.hasNext())
    {
      Integer I = (Integer)iter.next();
      OneInsertionLineView view = (OneInsertionLineView)views.get(I.intValue());
      view.unmark();
    }
    selection.clear();
  }
  
  public JComponent JComponent()
  {
    return myPanel;
  }
  
  
  
  private class MyItemListener implements InsertionModelList.ItemListener
  {

    public void itemAdded(InsertionModel model, int index)
    {
      addItem(model);
    }
    
  }
  
  private class MyViewChangeListener implements ViewChangeListener
  {

    public void viewShouldBeRemoved(View view)
    {
      removeItem((OneInsertionLineView)view);
    }
    
  }

  
  private class MyBroadcastListener extends BroadcastListener
  {
    public void broadcastFormControlModelSelection(BroadcastObjectSelection b) {}
    public void broadcastInsertionModelSelection(BroadcastObjectSelection b)
    { 
      if (b.getClearSelection()) clearSelection();
      InsertionModel model = (InsertionModel)b.getObject();
      for (int index = 0; index < views.size(); ++index)
      {
        OneInsertionLineView view = (OneInsertionLineView)views.get(index);
        if (view.getModel() == model)
        {
          int state = b.getState();
          if (state == 0) //toggle
            state = selection.contains(index) ? -1 : 1;
            
          switch (state)
          {
            case -1: //abw�hlen
                     view.unmark();
                     selection.remove(index);
                     break;
            case 1: //ausw�hlen
                     view.mark();
                     selection.add(index);
                     break;
          }
        }
      }

    }
  }
    
}