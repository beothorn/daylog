# Using Daylog

Daylog helps you remember what you did. You write a short note about what you are doing, and Daylog adds it to your diary file.

## Start
Run `daylog.jar` (it needs Java 21 or newer installed, see the [glossary](glossary.md#java)). An icon appears in the [tray](glossary.md#tray).
Nothing else is installed on your computer.

## Choose your diary file
Right click the tray icon and choose **Choose log file**. A file window opens in the folder of the file you chose last time.
Pick any text file (a new name creates it). Daylog remembers your choice. See [log file](glossary.md#log-file).

## Write a note
1. Click the tray icon. A window opens.
2. The first line is the [task](glossary.md#task), already filled with your last one. Change it if you like: as you type, Daylog suggests
   earlier tasks; the suggested part is selected, so keep typing to ignore it, or press **Tab** to accept it.
3. In the box below write what you are doing, as much as you want (it scrolls).
4. Press **Save** (or **Ctrl+Enter**). Press **Esc** to close without saving.

If you have not chosen a diary file yet, Daylog asks for one first.

## What gets written
Each note adds a line to the end of your diary file with the date and time (`year_month_day_hour_minute`), the task, and your text:

````
2026_11_19_23_55 `Offhour work`
```
Now I am writing the setup.
```
````

Your existing text in the file is never changed; notes are only added at the end.

## Quit
Right click the tray icon and choose **Quit**.

## Where Daylog keeps its own data
In a folder called `.daylog` in your home folder (on Windows, `daylog` inside the application data folder). It holds your remembered tasks and your chosen diary file.

## Problems
- No icon in the tray on Linux with GNOME: install an "AppIndicator" extension; GNOME hides tray icons by default.

See the [glossary](glossary.md) for all terms.
