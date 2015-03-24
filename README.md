# jGrid
A lightweight, scaleable Java (*SWT*) grid that can be used in pure SWT or Eclipse RCP applications.

The grid supports standard features such as: -

- Sorting (multiple columns).
- Filtering.
- Images.
- Styling (Fonts, colours, cell borders, etc.).
- Column resizing/repositioning (via drag-and-drop).
- Grouping (data can be grouped by aggregate/summary values).

See the Examples.java class to set-up and run the sample demo.

![Alt Examples Screenshot](https://github.com/GrandmasterTash/jGrid/blob/master/com.notlob.jgrid.examples/screenshot.jpg)

To use the grid you need to give specify 4 things: -

1. IGridContentProvider
2. IGridLabelProvider
3. Columns
4. Elements (data to show in a row).
