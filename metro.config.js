const {getDefaultConfig,mergeConfig}=require('@react-native/metro-config');
const fs=require('fs');
const path=require('path');
// A worktree may share dependencies through a junction outside Metro's project root.
const modules=fs.realpathSync(path.join(__dirname,'node_modules'));
module.exports=mergeConfig(getDefaultConfig(__dirname),{
  watchFolders:[modules],
  resolver:{nodeModulesPaths:[modules]},
});
