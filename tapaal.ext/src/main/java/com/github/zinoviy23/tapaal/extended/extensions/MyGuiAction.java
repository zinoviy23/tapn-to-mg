package com.github.zinoviy23.tapaal.extended.extensions;

import pipe.gui.action.GuiAction;

import java.awt.event.ActionEvent;

public class MyGuiAction extends GuiAction {
  public MyGuiAction() {
    super("kek", "lol");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    System.out.println("clicked");
  }
}
