import { Component } from '@adventure'
import { name } from './module.js'
const os = require('node:os');

function main() {
  console.log("My name is " + name)
  console.log(Component.text("Hello from adventure component").toString())
  console.log("Hello world from node! " + os.machine())
}

main()