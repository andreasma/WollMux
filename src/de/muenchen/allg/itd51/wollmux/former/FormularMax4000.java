/*
* Dateiname: FormularMax4000.java
* Projekt  : WollMux
* Funktion : Stellt eine GUI bereit zum Bearbeiten einer WollMux-Formularvorlage.
* 
* Copyright: Landeshauptstadt M�nchen
*
* �nderungshistorie:
* Datum      | Wer | �nderungsgrund
* -------------------------------------------------------------------
* 03.08.2006 | BNK | Erstellung
* 08.08.2006 | BNK | Viel Arbeit reingesteckt.
* -------------------------------------------------------------------
*
* @author Matthias Benkmann (D-III-ITD 5.1)
* @version 1.0
* 
*/
package de.muenchen.allg.itd51.wollmux.former;

import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import com.sun.star.text.XTextDocument;

import de.muenchen.allg.afid.UNO;
import de.muenchen.allg.itd51.parser.ConfigThingy;
import de.muenchen.allg.itd51.wollmux.FormDescriptor;
import de.muenchen.allg.itd51.wollmux.Logger;
import de.muenchen.allg.itd51.wollmux.dialog.Common;
import de.muenchen.allg.itd51.wollmux.former.DocumentTree.DropdownFormControl;
import de.muenchen.allg.itd51.wollmux.former.DocumentTree.FormControl;
import de.muenchen.allg.itd51.wollmux.former.DocumentTree.InsertionBookmark;
import de.muenchen.allg.itd51.wollmux.former.DocumentTree.TextRange;
import de.muenchen.allg.itd51.wollmux.former.DocumentTree.Visitor;

/**
 * Stellt eine GUI bereit zum Bearbeiten einer WollMux-Formularvorlage.
 *
 * @author Matthias Benkmann (D-III-ITD 5.1)
 */
public class FormularMax4000
{
  private static final int GENERATED_LABEL_MAXLENGTH = 30;
  
  /**
   * Wird als Label gesetzt, falls kein sinnvolles Label automatisch generiert werden
   * konnte.
   */
  private static final String NO_LABEL = "";
  
  /**
   * Wird tempor�r als Label gesetzt, wenn kein Label ben�tigt wird, weil es sich nur um
   * eine Einf�gestelle handelt, die nicht als Formularsteuerelement erfasst werden soll.
   */
  private static final String INSERTION_ONLY = "<<InsertionOnly>>";
  
  /**
   * URL des Quelltexts f�r den Standard-Empf�ngerauswahl-Tab.
   */
  private final URL EMPFAENGER_TAB_URL = this.getClass().getClassLoader().getResource("data/empfaengerauswahl_controls.conf");
  
  /**
   * URL des Quelltexts f�r die Standardbuttons f�r einen mittleren Tab.
   */
  private final URL STANDARD_BUTTONS_MIDDLE_URL = this.getClass().getClassLoader().getResource("data/standardbuttons_mitte.conf");
  
  /**
   * URL des Quelltexts f�r die Standardbuttons f�r den letzten Tab.
   */
  private final URL STANDARD_BUTTONS_LAST_URL = this.getClass().getClassLoader().getResource("data/standardbuttons_letztes.conf");
  
  //TODO MAGIC_DESCRIPTOR_PATTERN in FormularMax 4000 Doku dokumentieren
  private static final Pattern MAGIC_DESCRIPTOR_PATTERN = Pattern.compile("\\A(.*)<<(.*)>>\\z");

  /**
   * ActionListener f�r Buttons mit der ACTION "abort". 
   */
  private ActionListener actionListener_abort = new ActionListener()
     { public void actionPerformed(ActionEvent e){ abort(); } };

  /**
   * wird getriggert bei windowClosing() Event.
   */
  private ActionListener closeAction = actionListener_abort;

  /**
   * Das Haupt-Fenster des FormularMax4000.
   */
  private JFrame myFrame;
  
  private String formTitle = "Generiert durch FormularMax 4000";
  
  private FormControlModelList formControlModelList = new FormControlModelList();
  
  private FormDescriptor formDescriptor;
  
  public FormularMax4000(final XTextDocument doc)
  {
    formDescriptor = new FormDescriptor(doc);
    init();
    
     //  GUI im Event-Dispatching Thread erzeugen wg. Thread-Safety.
    try{
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
            try{createGUI(doc);}catch(Exception x){Logger.error(x);};
        }
      });
    }
    catch(Exception x) {Logger.error(x);}
  }
  
  private void createGUI(final XTextDocument doc)
  {
    Common.setLookAndFeelOnce();
    
    //  Create and set up the window.
    myFrame = new JFrame("FormularMax 4000");
    //leave handling of close request to WindowListener.windowClosing
    myFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    MyWindowListener oehrchen = new MyWindowListener();
    //der WindowListener sorgt daf�r, dass auf windowClosing mit abort reagiert wird
    myFrame.addWindowListener(oehrchen);
    
    JPanel myPanel = new JPanel(new GridLayout(1, 2));
    myFrame.getContentPane().add(myPanel);
    JMenuBar mbar = new JMenuBar();
    
    //========================= Datei ============================
    JMenu menu = new JMenu("Datei");
    JMenuItem menuItem = new JMenuItem("Formularfelder aus Dokument einlesen");
    menuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e)
      {
        scan(doc);
      }});
    menu.add(menuItem);
    
    menuItem = new JMenuItem("Formularbeschreibung ins Dokument �bertragen");
    menuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e)
      {
        writeFormDescriptor();
      }});
    menu.add(menuItem);
    
    mbar.add(menu);
//  ========================= Formular ============================
    menu = new JMenu("Formular");
    menuItem = new JMenuItem("Empf�ngerauswahl-Tab einf�gen");
    menuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e)
      {
        insertStandardEmpfaengerauswahl();
      }
      });
    menu.add(menuItem);
    
    menuItem = new JMenuItem("Standardbuttons (mittlere Tabs) einf�gen");
    menuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e)
      {
        insertStandardButtonsMiddle();
      }
      });
    menu.add(menuItem);
    
    menuItem = new JMenuItem("Standardbuttons (letzter Tab) einf�gen");
    menuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e)
      {
        insertStandardButtonsLast();
      }
      });
    menu.add(menuItem);
    
    menuItem = new JMenuItem("Formularbeschreibung editieren");
    menuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e)
      {
        editFormDescriptor();
      }});
    menu.add(menuItem);

    
    mbar.add(menu);
    
    
    
    myFrame.setJMenuBar(mbar);
    
    myFrame.pack();
    myFrame.setResizable(true);
    myFrame.setVisible(true);
  }
  
  /**
   * Wertet {@link #formDescriptor} aus aus und initialisiert alle internen
   * Strukturen entsprechend.
   * @author Matthias Benkmann (D-III-ITD 5.1)
   * TESTED
   */
  private void init()
  {
    formControlModelList.clear();
    ConfigThingy conf = formDescriptor.toConfigThingy();
    parseGlobalFormInfo(conf);
    
    ConfigThingy fensterAbschnitte = conf.query("Formular").query("Fenster");
    Iterator fensterAbschnittIterator = fensterAbschnitte.iterator();
    while (fensterAbschnittIterator.hasNext())
    {
      ConfigThingy fensterAbschnitt = (ConfigThingy)fensterAbschnittIterator.next();
      Iterator tabIter = fensterAbschnitt.iterator();
      while (tabIter.hasNext())
      {
        ConfigThingy tab = (ConfigThingy)tabIter.next();
        parseTab(tab, -1);
      }
    }
//  TODO writeFormDescriptor();
  }
  
  /**
   * Speichert die Formularbeschreibung in einem Benutzerfeld der DocumentInfo.
   * 
   * @author Matthias Benkmann (D-III-ITD 5.1)
   * TESTED
   */
  private void writeFormDescriptor()
  {
    ConfigThingy conf = exportFormDescriptor();
    formDescriptor.fromConfigThingy(conf);
    formDescriptor.writeDocInfoFormularbeschreibung();
  }

  /**
   * Liefert ein ConfigThingy zur�ck, das den aktuellen Zustand der Formularbeschreibung
   * repr�sentiert.
   * @author Matthias Benkmann (D-III-ITD 5.1)
   */
  private ConfigThingy exportFormDescriptor()
  {
    ConfigThingy conf = new ConfigThingy("WM");
    ConfigThingy form = conf.add("Formular");
    form.add("TITLE").add(formTitle);
    form.addChild(formControlModelList.export());
    return conf;
  }
  
  /**
   * Extrahiert aus conf die globalen Eingenschaften des Formulars wie z,B, den Formulartitel.
   * @param conf der WM-Knoten der �ber einer beliebigen Anzahl von Formular-Knoten sitzt.
   * @author Matthias Benkmann (D-III-ITD 5.1)
   * TESTED
   */
  private void parseGlobalFormInfo(ConfigThingy conf)
  {
    ConfigThingy tempConf = conf.query("Formular").query("TITLE");
    if (tempConf.count() > 0) formTitle = tempConf.toString();
  }
  
  /**
   * F�gt am Anfang der Liste eine Standard-Empfaengerauswahl-Tab ein.
   * 
   * @author Matthias Benkmann (D-III-ITD 5.1)
   */
  private void insertStandardEmpfaengerauswahl()
  {
    try{ 
      ConfigThingy conf = new ConfigThingy("Empfaengerauswahl", EMPFAENGER_TAB_URL);
      int rand = (int)(Math.random()*100); 
      FormControlModel separatorTab = FormControlModel.createTab("Eingabe", "Reiter"+rand);
      formControlModelList.add(separatorTab,0);
      parseTab(conf, 0);
      //TODO writeFormDescriptor();
    }catch(Exception x) {}
  }
  
  /**
   * H�ngt die Standardbuttons f�r einen mittleren Tab an das Ende der Liste.
   * 
   * @author Matthias Benkmann (D-III-ITD 5.1)
   */
  private void insertStandardButtonsMiddle()
  {
    try{ 
      ConfigThingy conf = new ConfigThingy("Buttons", STANDARD_BUTTONS_MIDDLE_URL);
      parseGrandchildren(conf, -1);
      //TODO writeFormDescriptor();
    }catch(Exception x) {}
  }
  
  /**
   * H�ngt die Standardbuttons f�r den letzten Tab an das Ende der Liste.
   * 
   * @author Matthias Benkmann (D-III-ITD 5.1)
   */
  private void insertStandardButtonsLast()
  {
    try{ 
      ConfigThingy conf = new ConfigThingy("Buttons", STANDARD_BUTTONS_LAST_URL);
      parseGrandchildren(conf, -1);
      //TODO writeFormDescriptor();
    }catch(Exception x) {}
  }
  
  /**
   * Parst das Tab conf und f�gt entsprechende FormControlModels der 
   * {@link #formControlModelList} hinzu.
   * @param conf der Knoten direkt �ber "Eingabefelder" und "Buttons".
   * @param idx falls >= 0 werden die Steuerelemente am entsprechenden Index der
   *        Liste in die Formularbeschreibung eingef�gt, ansonsten ans Ende angeh�ngt.
   * @author Matthias Benkmann (D-III-ITD 5.1)
   * TESTED
   */
  private void parseTab(ConfigThingy conf, int idx)
  {
    String id = conf.getName();
    String label = id;
    String action = FormControlModel.NO_ACTION;
    String tooltip = "";
    char hotkey = 0;
    
    Iterator iter = conf.iterator();
    while (iter.hasNext())
    {
      ConfigThingy attr = (ConfigThingy)iter.next();
      String name = attr.getName();
      String str = attr.toString();
      if (name.equals("TITLE")) label = str; 
      else if (name.equals("CLOSEACTION")) action = str;
      else if (name.equals("TIP")) tooltip = str;
      else if (name.equals("HOTKEY")) hotkey = str.length() > 0 ? str.charAt(0) : 0;
    }
    
    FormControlModel tab = FormControlModel.createTab(label, id);
    tab.setAction(action);
    tab.setTooltip(tooltip);
    tab.setHotkey(hotkey);
    
    if (idx >= 0)
    {
      formControlModelList.add(tab, idx++);
      idx += parseGrandchildren(conf.query("Eingabefelder"), idx);
      parseGrandchildren(conf.query("Buttons"), idx);
    }
    else
    {
      formControlModelList.add(tab);
      parseGrandchildren(conf.query("Eingabefelder"), -1);
      parseGrandchildren(conf.query("Buttons"), -1);
    }
    
    
    
  }
  
  /**
   * Parst die Kinder der Kinder von grandma als Steuerelemente und f�gt der
   * {@link #formControlModelList} entsprechende FormControlModels hinzu.
   * @param idx falls >= 0 werden die Steuerelemente am entsprechenden Index der
   *        Liste in die Formularbeschreibung eingef�gt, ansonsten ans Ende angeh�ngt.
   * @return die Anzahl der erzeugten Steuerelemente.
   * @author Matthias Benkmann (D-III-ITD 5.1)
   * TESTED
   */
  private int parseGrandchildren(ConfigThingy grandma, int idx)
  {
    int count = 0;
    Iterator grandmaIter = grandma.iterator();
    while (grandmaIter.hasNext())
    {
      Iterator iter = ((ConfigThingy)grandmaIter.next()).iterator();
      while (iter.hasNext())
      {
        FormControlModel model = new FormControlModel((ConfigThingy)iter.next());
        ++count;
        if (idx >= 0)
          formControlModelList.add(model, idx++);
        else
          formControlModelList.add(model);
      }
    }
    return count;
  }
  
  
  
  private void scan(XTextDocument doc)
  {
    try{
      DocumentTree tree = new DocumentTree(doc);
      Visitor visitor = new DocumentTree.Visitor(){
        private Map insertions = new HashMap();
        private StringBuilder text = new StringBuilder();
        private FormControlModel fixupCheckbox = null;
        
        public boolean container(int count)
        {
          if (fixupCheckbox != null && fixupCheckbox.getLabel() == NO_LABEL)
            fixupCheckbox.setLabel(makeLabelFromStartOf(text, 2*GENERATED_LABEL_MAXLENGTH));
          text.setLength(0);
          fixupCheckbox = null;
          return true;
        }
        
        public boolean textRange(TextRange textRange)
        {
          text.append(textRange.getString());
          return true;
        }
        
        public boolean insertionBookmark(InsertionBookmark bookmark)
        {
          if (bookmark.isStart())
            insertions.put(bookmark.getName(), bookmark);
          else
            insertions.remove(bookmark.getName());
          
          return true;
        }
        
        public boolean formControl(FormControl control)
        {
          if (insertions.isEmpty())
          {
            FormControlModel model = registerFormControl(control, text);
            if (model != null && model.getType() == FormControlModel.CHECKBOX_TYPE)
              fixupCheckbox = model;
          }
          
          return true;
        }
      };
      visitor.visit(tree);
    } 
    catch(Exception x) {Logger.error("Fehler w�hrend des Scan-Vorgangs",x);}
  }
  
  //text: Text der im selben Absatz wie das Control vor dem Control steht.
  private FormControlModel registerFormControl(FormControl control, StringBuilder text)
  {
    String label;
    String id;
    String descriptor = control.getDescriptor();
    Matcher m = MAGIC_DESCRIPTOR_PATTERN.matcher(descriptor);
    if (m.matches())
    {
      label = m.group(1);
      if (label.length() == 0) label = INSERTION_ONLY; //TODO Magic Dokumentieren
      id = m.group(2);
    }
    else
    {
      label = makeLabelFromEndOf(text, GENERATED_LABEL_MAXLENGTH);
      id = descriptor;
    }
    
    id = makeControlId(label, id);
    
    switch (control.getType())
    {
      case DocumentTree.CHECKBOX_CONTROL: return registerCheckbox(control, label, id);
      case DocumentTree.DROPDOWN_CONTROL: return registerDropdown((DropdownFormControl)control, label, id);
      case DocumentTree.INPUT_CONTROL:    return registerInput(control, label, id);
      default: Logger.error("Unbekannter Typ Formular-Steuerelement"); return null;
    }
  }

  /**
   * Bastelt aus dem Ende des Textes text ein Label.
   * @author Matthias Benkmann (D-III-ITD 5.1)
   * @param maxlen TODO
   */
  private String makeLabelFromEndOf(StringBuilder text, int maxlen)
  {
    String label;
    String str = text.toString().trim();
    int len = str.length();
    if (len > maxlen) len = maxlen;
    label = str.substring(str.length() - len);
    if (label.length() < 2) label = NO_LABEL;
    return label;
  }
  
  /**
   * Bastelt aus dem Start des Textes text ein Label.
   * @author Matthias Benkmann (D-III-ITD 5.1)
   */
  private String makeLabelFromStartOf(StringBuilder text, int maxlen)
  {
    String label;
    String str = text.toString().trim();
    int len = str.length();
    if (len > maxlen) len = maxlen;
    label = str.substring(0, len);
    if (label.length() < 2) label = NO_LABEL;
    return label;
  }
  
  private FormControlModel registerCheckbox(FormControl control, String label, String id)
  {
    FormControlModel model = null;
    if (label != INSERTION_ONLY)
    {
      model = FormControlModel.createCheckbox(label, id);
      if (control.getString().equalsIgnoreCase("true"))
      {
        ConfigThingy autofill = new ConfigThingy("AUTOFILL");
        autofill.add("true");
        model.setAutofill(autofill);
      }
      formControlModelList.add(model);
    }
    
    control.surroundWithBookmark(insertFormValue(id));
    return model;
  }
  
  private FormControlModel registerDropdown(DropdownFormControl control, String label, String id)
  {
    FormControlModel model = null;
    if (label != INSERTION_ONLY)
    {
      String[] items = control.getItems();
      boolean editable = false;
      for (int i = 0; i < items.length; ++i)
      {
        if (items[i].equalsIgnoreCase("<<Freitext>>")) //TODO Diese Magic dokumentieren 
        {
          String[] newItems = new String[items.length - 1];
          System.arraycopy(items, 0, newItems, 0, i);
          System.arraycopy(items, i + 1, newItems, i, items.length - i - 1);
          items = newItems;
          editable = true;
          break;
        }
      }
      model = FormControlModel.createComboBox(label, id, items);
      model.setEditable(editable);
      String preset = control.getString().trim();
      if (preset.length() > 0)
      {
        ConfigThingy autofill = new ConfigThingy("AUTOFILL");
        autofill.add(preset);
        model.setAutofill(autofill);
      }
      formControlModelList.add(model);
    }
    
    control.surroundWithBookmark(insertFormValue(id));
    return model;
  }
  
  private FormControlModel registerInput(FormControl control, String label, String id)
  {
    FormControlModel model = null;
    if (label != INSERTION_ONLY)
    {
      model = FormControlModel.createTextfield(label, id);
      String preset = control.getString().trim();
      if (preset.length() > 0)
      {
        ConfigThingy autofill = new ConfigThingy("AUTOFILL");
        autofill.add(preset);
        model.setAutofill(autofill);
      }
      formControlModelList.add(model);
    }
    
    control.surroundWithBookmark(insertFormValue(id));
    return model;
  }
  
  /**
   * Macht aus str einen passenden Bezeichner f�r ein Steuerelement. Falls label == "", so
   * muss der Bezeichner nicht eindeutig sein (dies ist der Marker f�r eine reine
   * Einf�gestelle, f�r die kein Steuerelement erzeugt werden muss).
   * @author Matthias Benkmann (D-III-ITD 5.1)
   * TODO Testen
   */
  private String makeControlId(String label, String str)
  {
    str = str.replaceAll("[^a-zA-Z_0-9]","");
    if (str.length() == 0) str = "Steuerelement";
    if (!str.matches("^[a-zA-Z_].*")) str = "_" + str;
    if (label.length() > 0)
      return formControlModelList.makeUniqueId(str);
    else
      return str;
  }

  private void editFormDescriptor()
  {
    final JFrame editorFrame = new JFrame("Formularbeschreibung bearbeiten");
    editorFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    final JEditorPane editor = new JEditorPane("text/plain", exportFormDescriptor().stringRepresentation());
    JScrollPane scrollPane = new JScrollPane(editor, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    editorFrame.setContentPane(scrollPane);
    editorFrame.addWindowListener(new WindowAdapter()
        {
          ConfigThingy conf;
          public void windowClosing(WindowEvent e) 
          {
            try
            {
              conf = new ConfigThingy("", null, new StringReader(editor.getText()));
              editorFrame.dispose();
            }
            catch (Exception e1)
            {
              JOptionPane.showMessageDialog(editorFrame, e1.getMessage(), "Fehler beim Parsen der Formularbeschreibung", JOptionPane.WARNING_MESSAGE);
            }
          }
          public void windowClosed(WindowEvent e)
          {
            formDescriptor.fromConfigThingy(conf);
            init();
          }
        });
    
    
    editorFrame.pack();
    editorFrame.setVisible(true);
    editorFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
  }
  
  /**
   * Liefert "WM(CMD'insertFormValue' ID '&lt;id>').
   * @author Matthias Benkmann (D-III-ITD 5.1)
   */
  private String insertFormValue(String id)
  {
    return "WM(CMD'insertFormValue' ID '"+id+"')";
  }
  
  /**
   * Implementiert die gleichnamige ACTION.
   * 
   * @author Matthias Benkmann (D-III-ITD 5.1)
   */
  private void abort()
  {
    myFrame.dispose();
  }
  
  private class MyWindowListener implements WindowListener
  {
    public void windowOpened(WindowEvent e) {}
    public void windowClosing(WindowEvent e) {closeAction.actionPerformed(null); }
    public void windowClosed(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e){}   
    
  }
  
  /**
   * Ruft den FormularMax4000 f�r das aktuelle Vordergrunddokument auf, falls dieses
   * ein Textdokument ist. 
   * 
   * @author Matthias Benkmann (D-III-ITD 5.1)
   */
  public static void main(String[] args) throws Exception
  {
    UNO.init();
    XTextDocument doc = UNO.XTextDocument(UNO.desktop.getCurrentComponent());
    new FormularMax4000(doc);
  }

}