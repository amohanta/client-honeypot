--------------------HoneySift Project for CS5231 Module NUS-----------------------------------------------------
----------------------------------README.TXT--------------------------------------------------------------------

Requirements:
=============
That program is written in java and requires the  Java(TM) SE Runtime Environment (build 1.6.0_20-b02) to be installed or any more up to date Sun JDK/JRE version.

That program needs to be run under linux with the library "libdisasm version 0.23" or later to have full functionnalities 
(the headers for that library as system calls are used to access it)
The program can also run under windows but will not provide results for the cosine similarity as this function requires binary dissassembly perfomed with libdisasm.

First you should copy the executable jar Honeysift.jar and the Sqlite database MalDB in the same folder.
For any web pages to you want to specifically analyze you can create a subdirectory and paste them there.

In order to be able to view the results you should install a database viewer for sqlite http://www.sqlite.org/download.html

Results will be in the table "Report"
You should not modify other tables as they wera buyilt for the machine learning approach.

Compilation:
====================

No compilation required :)


Utilization:
============

You need to have root privilege to execute the program.

The command line is:

    $ java -jar Honeysift.jar --help
   

Run it:
=======

   $ python phoneyc.py URL-you-what-to-examine

You should get this help:

--------------------HoneySift Project for CS5231 Module NUS-----------------------------------------------------
Option                                  Description                            
------                                  -----------                            
-? [help]                                                                      
--accuracy <Double: accuracy 0<x<1 for  (default: 0.96)                        
  the cosine similiraty to tag the                                             
  code as malicious>                                                           
--analyze [Analyzing : select this                                             
  option for the static analysis to                                            
  run]                                                                         
--crawl <crawl : select this option to  (default: http://forum.malekal.com)    
  crawl the web>                                                               
--depth <Integer: depth of search for   (default: 1)                           
  crawling, warning depth>2 can be                                             
  very long>                                                                   
--filecrawl [crawl : select this        (default: D:                           
  option to crawl the web based on the    \Users\Damien\Documents\FastHoneyPot\seeds.
  URLS that are the file that you         txt)                                 
  provide]                                                                     
--help [help]                                                                  
--input [Input folder for learning or   (default: Captures)                    
  analyzing web pages (no need to                                              
  change if you use crawling). The                                             
  folder must be a subfolder of the                                            
  location where the program is and                                            
  you should give only the subpath]                                            
--learning <learn : used when you want  (default: Obfuscated)                  
  to add normal or obfuscated code to                                          
  the learning database {Normal,                                               
  Obfuscated}>                                                                 
--verbose [be verbose: display the                                             
  javascripts that are extracted and                                           
  more information]                                                            
 
Examples of valid commands are:
--crawl http://www.facebook.com --depth 2 --analyze
--filecrawl list.txt --analyze --accuracy 0.98
--analyze --input test\malwares
--learn Normal --input test\NormalPages

Additional Information:
=======================

After simply follow the commands.

Warning: if you get the message "Page could not be downloaded, file was deleted go to next URL" during ceawling this is normal 
as most pages that are crawls require credentials or are generated dynamically  and thus cannot always be downloaded.
Pages under the protocol https cannot be crawled.