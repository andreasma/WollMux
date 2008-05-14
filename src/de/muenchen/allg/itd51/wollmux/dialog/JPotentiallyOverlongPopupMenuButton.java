//TODO L.m()
/*
* Dateiname: JPotentiallyOverlongPopupMenuButton.java
* Projekt  : WollMux
* Funktion : Stellt einen Button dar mit einem Popup-Men�, das darauf vorbereitet ist, sehr viele Elemente anzubieten.
* 
 * Copyright (c) 2008 Landeshauptstadt M�nchen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the European Union Public Licence (EUPL),
 * version 1.0.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * European Union Public Licence for more details.
 *
 * You should have received a copy of the European Union Public Licence
 * along with this program. If not, see
 * http://ec.europa.eu/idabc/en/document/7330/5980
*
* �nderungshistorie:
* Datum      | Wer | �nderungsgrund
* -------------------------------------------------------------------
* 13.02.2008 | BNK | Erstellung
* -------------------------------------------------------------------
*
* @author Matthias Benkmann (D-III-ITD D.10)
* @version 1.0
* 
*/
package de.muenchen.allg.itd51.wollmux.dialog;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPopupMenu;

/**
 * Stellt einen Button dar mit einem Popup-Men�, das darauf vorbereitet ist,
 * sehr viele Elemente anzubieten.
 * TODO: Das Behandeln von �berlangen Men�s muss noch implementiert werden.
 * 
 * @author Matthias Benkmann (D-III-ITD D.10)
 */
public class JPotentiallyOverlongPopupMenuButton extends JButton
{
  /**
   * Macht Eclipse gl�cklich.
   */
  private static final long serialVersionUID = 3206786778925266706L;

  /**
   * Erzeugt einen Button mit Beschriftung label, bei dessen Bet�tigung
   * eine Popup-Men� erscheint, in dem alle Elemente aus actions enthalten sind.
   * Wenn das Popup-Men� zu lang w�re, um auf den Bildschirm zu passen, passiert
   * etwas intelligentes.
   * Die Elemente von actions k�nnen {@link javax.swing.Action} oder
   * {@link java.awt.Component} Objekte sein.
   * ACHTUNG! Bei jeder Bet�tigung des Buttons wird das Men� neu aufgebaut,
   * d.h. wenn sich die actions �ndert, �ndert sich das Men�.
   * @author Matthias Benkmann (D-III-ITD D.10)
   */
  public JPotentiallyOverlongPopupMenuButton(String label, final Iterable actions)
  {
    super(label);

    this.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        JPopupMenu menu = new JPopupMenu();
        Iterator iter = actions.iterator();
        while (iter.hasNext())
        {
          Object action = iter.next();
          if (action instanceof Action)
            menu.add((Action)action);
          else
            menu.add((Component)action);
        }
      
        menu.show(JPotentiallyOverlongPopupMenuButton.this, 0, JPotentiallyOverlongPopupMenuButton.this.getSize().height);
      }
    });
  }
}
