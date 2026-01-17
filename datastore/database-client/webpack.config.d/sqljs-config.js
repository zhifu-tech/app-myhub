// {project}/webpack.config.d/sqljs.js
config.resolve = {
  fallback: {
    fs: false,
    path: false,
    crypto: false,
  }
};

// 检查是否已经添加了 CopyWebpackPlugin（避免重复添加）
// 使用全局标记来避免重复加载
if (!global.__sqljsConfigLoaded) {
  global.__sqljsConfigLoaded = true;
  
  const CopyWebpackPlugin = require('copy-webpack-plugin');
  config.plugins.push(
    new CopyWebpackPlugin({
      patterns: [
        '../../node_modules/sql.js/dist/sql-wasm.wasm'
      ]
    })
  );
}

