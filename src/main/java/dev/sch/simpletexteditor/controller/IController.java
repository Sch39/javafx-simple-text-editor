package dev.sch.simpletexteditor.controller;

import javafx.scene.Node;

public interface IController<T extends Node> {
    T getView();
    void initialize();
}
