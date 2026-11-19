# Using Daylog

Daylog helps you remember what you did. You write a short note about what you are doing, and Daylog adds it to your diary file.

## Start
Run `daylog.jar` (it needs Java 21 or newer installed, see the [glossary](glossary.md#java)). An icon appears in the [tray](glossary.md#tray).
Nothing else is installed on your computer.

## Choose your diary file
Right click the tray icon and choose **Choose log file**. A file window opens in the folder of the file you chose last time.
Pick any text file (a new name creates it). Daylog remembers your choice. See [log file](glossary.md#log-file).

## Write a note
1. Click the tray icon (a small book). A window opens with the cursor already in the big box.
2. In that box write what you are doing, as much as you want (it scrolls).
3. The first line is the [task](glossary.md#task), already filled with your last one. To change it, click it and type: a list of earlier tasks
   that start with what you typed drops down. Choose one with the **Up/Down** arrows and **Enter**, or with the mouse. **Esc** closes the list.
4. Press **Enter** to save. To start a new line inside your text, press **Shift+Enter**. Press **Esc** to close without saving.

The right click menu also shows the Daylog [version](glossary.md#version) you are using.

If you have not chosen a diary file yet, Daylog asks for one first.

## What gets written
Each note adds a few lines to the end of your diary file, after one empty line: the date and time (`year_month_day_hour_minute`), the task, and your text:

````
2026_11_19_23_55 `Offhour work`
```
Now I am writing the setup.
```
````

Your existing text in the file is never changed; notes are only added at the end, each separated from the previous one by an empty line.

## Quit
Right click the tray icon and choose **Quit**.

## Where Daylog keeps its own data
In a folder called `.daylog` in your home folder (on Windows, `daylog` inside the application data folder). It holds your remembered tasks and your chosen diary file.

## Problems
- No icon in the tray on Linux with GNOME: install an "AppIndicator" extension; GNOME hides tray icons by default.

See the [glossary](glossary.md) for all terms.
