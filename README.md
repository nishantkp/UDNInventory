# UDNInventory - An Inventory Management App

Project as a part of nano-degree at [Udacity](https://www.udacity.com).
This is the capstone project of the Nanodegree program.

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
- TextInputLayout
- Create personal profile
- Login

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

#### TextInputLayout

Make your EditText view as per material design guidlines.

###### Import support libraries

```gradle
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'com.android.support:appcompat-v7:26.1.0'
    implementation 'com.android.support:design:26.1.0'
}
```

`TextInputLayout` just works as `LinearLayout`, it's just a wrapper. It only excepts one child similar to `ScrollView`.

```xml
<android.support.design.widget.TextInputLayout
    android:id="@+id/emailWrapper"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
 
    <EditText
        android:id="@+id/user_email"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:inputType="textEmailAddress"
        android:hint="Email"/>
   
</android.support.design.widget.TextInputLayout>
```
Initialize reference to `TextInutLayout`.

```java
final TextInputLayout emailWrapper = (TextInputLayout) findViewById(R.id.emailWrapper);
```
To animate floating lable as per material design guide lines, use `setHint` method,
```java
emailWrapper.setHint("Email");
```

#### Login Screen

Used generic-login as per material design guide-lines.
For getting reference [click here](https://github.com/nishantkp/generic-login).

## UX Design

<img src="https://user-images.githubusercontent.com/32653955/34083100-d88841a8-e338-11e7-89d0-43ad553772f9.png" width="275" height="475"> <img src="https://user-images.githubusercontent.com/32653955/34083102-d89fbf40-e338-11e7-8abc-a0c090006a32.png" width="275" height="475"> <img src="https://user-images.githubusercontent.com/32653955/34083101-d8947ed2-e338-11e7-8653-3ba05ad3174a.png" width="275" height="475"> <img src="https://user-images.githubusercontent.com/32653955/34085934-54c7c8b6-e365-11e7-9744-12078c22e33b.png" width="275" height="475"> <img src="https://user-images.githubusercontent.com/32653955/34085979-b65e15e4-e365-11e7-9d56-2f61a4f53a71.png" width="275" height="475"> <img src="https://user-images.githubusercontent.com/32653955/34085978-b3184d78-e365-11e7-801b-db3c99ff03a7.png" width="275" height="475"> <img src="https://user-images.githubusercontent.com/32653955/34086006-1d69ccd8-e366-11e7-99aa-85a4b5ac40ad.png" width="275" height="475">

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
