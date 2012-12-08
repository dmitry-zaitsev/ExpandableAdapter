ExpandableAdapter
=================

ListAdapter that makes an expandable list with animation from your ListView. ExpandableAdapter wraps around your ListAdapter (so there is no need to change a lot of code) and uses it's custom wrapper view to provide great perfomance while playing animation (it's doesn't matter how complex your layout is).

Installation
============

Add .jar to build path.

Usage
=====

All you have to do is wrap ExapndableAdapter around your own adapter, providing it id of switch view (that will show\close second level on click) and holder view (that holds content that you want to show\hide by click).

  ListAdapter adapter = new ExpandableAdapter(context,
									yourAdapter,
									R.id.switch,
									R.id.holder);
	listView.setAdapter(adapter);
	
Done!

ExpandableAdapter will automatically hide holder at startup and set onClick listener for switch view.

License
=======

	Copyright dmitry.zaicew@gmail.com Dmitry Zaitsev

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	    http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License	