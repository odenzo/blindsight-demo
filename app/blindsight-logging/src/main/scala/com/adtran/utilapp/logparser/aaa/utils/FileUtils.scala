package com.adtran.utilapp.logparser.aaa.utils

import fs2.io.file.Path

def removeExtensions(file: fs2.io.file.Path): Path = {
  val ext = file.fileName.extName
  file.resolveSibling(file.fileName.toString.dropRight(ext.length + 1))
}
