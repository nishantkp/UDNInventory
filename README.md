# UDNInventory - An Inventory Management App

Project as a part of nano-degree at [Udacity](https://www.udacity.com).
<br>This is the capstone project of the Nanodegree program.</br>

### Project Rubic

The goal is to design and create the structure of an Inventory App which would allow a store to keep track of its inventory. 
Imagine that you are the Android developer at a merchandise startup, tasked with building out their inventory app. Consider the user workflow, user needs, and backend database storage needs as you plan.

#### Helpers :smiley:

- SQLite database
- Camera Intent
- CRUD operations
- Alert dialog
- Cursor adaptor
- Cursor loader
- SQLiteOpenHelper
- AsyncTask Runnable
- Coordinator layout

#### Features

- Add new product
- Delete existing product
- View summary of product
- Capture image of product
- Make an order of existing product with one click
- Decrease the quantity of product from homescreen

#### Perform a small background task with AsyncTask

If you need to:
- Execute code on a background Thread
- Task that DOES NOT update UI
- Task which will take at most few seconds to complete

```java
AsyncTask.execute(new Runnable() {
   @Override
   public void run() {
      //TODO your background code
   }
});
```

## PROJECT LICENSE

```
This project was submitted by Nishant Patel as part of the Nanodegree At Udacity.

As part of Udacity Honor code, your submissions must be your own work, hence
submitting this project as yours will cause you to break the Udacity Honor Code
and the suspension of your account.

Me, the author of the project, allow you to check the code as a reference, but if
you submit it, it's your own responsibility if you get expelled.

MIT License

Besides the above notice, the following license applies and this license notice
must be included in all works derived from this project.

Copyright (c) 2017 Nishant Patel

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
