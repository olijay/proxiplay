function nodePowerCmp(a,b) {  

    return a.power > b.power;
}

function nodeReversePowerCmp(a,b) {
    return a.power < b.power;
}

function nodePowerCompare(a,b) {
  if (a.power < b.power)
     return -1;
  if (a.power > b.power)
    return 1;
  return 0;
}