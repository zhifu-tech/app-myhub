// {project}/webpack.config.d/skiko-config.js
// 配置 webpack 以正确处理 skiko.mjs 模块

const path = require('path');
const fs = require('fs');
const webpack = require('webpack');
const CopyWebpackPlugin = require('copy-webpack-plugin');

// 配置 resolve
config.resolve = config.resolve || {};
config.resolve.alias = config.resolve.alias || {};
config.resolve.modules = config.resolve.modules || [];

// 查找 skiko.mjs 文件的位置
const buildJsPath = path.resolve(__dirname, '../../../build/js');
const packagesImportedPath = path.join(buildJsPath, 'packages_imported');

// 查找 skiko.mjs 文件
function findSkikoMjs() {
  // 优先查找 components-resources-js（Compose Resources 通常包含 skiko）
  const componentsResourcesPath = path.join(packagesImportedPath, 'components-resources-js');
  if (fs.existsSync(componentsResourcesPath)) {
    const versions = fs.readdirSync(componentsResourcesPath).filter(f => 
      fs.statSync(path.join(componentsResourcesPath, f)).isDirectory()
    );
    if (versions.length > 0) {
      // 使用最新版本
      const latestVersion = versions.sort().reverse()[0];
      const versionPath = path.join(componentsResourcesPath, latestVersion);
      const skikoMjsPath = path.join(versionPath, 'skiko.mjs');
      if (fs.existsSync(skikoMjsPath)) {
        return { path: skikoMjsPath, dir: versionPath };
      }
    }
  }
  
  // 尝试从 skiko-js 包中查找
  const skikoJsPath = path.join(packagesImportedPath, 'skiko-js');
  if (fs.existsSync(skikoJsPath)) {
    const versions = fs.readdirSync(skikoJsPath).filter(f => 
      fs.statSync(path.join(skikoJsPath, f)).isDirectory()
    );
    if (versions.length > 0) {
      const latestVersion = versions.sort().reverse()[0];
      const versionPath = path.join(skikoJsPath, latestVersion);
      const skikoMjsPath = path.join(versionPath, 'skiko.mjs');
      if (fs.existsSync(skikoMjsPath)) {
        return { path: skikoMjsPath, dir: versionPath };
      }
    }
  }
  
  // 如果找不到，尝试从其他包含 skiko 的包中查找
  if (fs.existsSync(packagesImportedPath)) {
    const packages = fs.readdirSync(packagesImportedPath);
    for (const pkg of packages) {
      const pkgPath = path.join(packagesImportedPath, pkg);
      if (fs.statSync(pkgPath).isDirectory()) {
        const versions = fs.readdirSync(pkgPath).filter(f => 
          fs.statSync(path.join(pkgPath, f)).isDirectory()
        );
        for (const version of versions.sort().reverse()) {
          const versionPath = path.join(pkgPath, version);
          const skikoMjsPath = path.join(versionPath, 'skiko.mjs');
          if (fs.existsSync(skikoMjsPath)) {
            return { path: skikoMjsPath, dir: versionPath };
          }
        }
      }
    }
  }
  
  return null;
}

const skikoInfo = findSkikoMjs();

if (skikoInfo) {
  // 方法1: 使用 NormalModuleReplacementPlugin 替换模块导入
  // 这会将所有对 ./skiko.mjs 的相对路径导入替换为绝对路径
  config.plugins = config.plugins || [];
  
  // 创建一个正则表达式来匹配 ./skiko.mjs 导入
  // 注意：需要匹配从 kotlin 目录中的文件导入的情况
  config.plugins.push(
    new webpack.NormalModuleReplacementPlugin(
      /^\.\/skiko\.mjs$/,
      function(resource) {
        // 替换为找到的 skiko.mjs 绝对路径
        resource.request = skikoInfo.path;
      }
    )
  );
  
  // 方法2: 配置 resolve.alias（作为备用）
  // 注意：alias 可能不会匹配相对路径，但保留作为备用
  config.resolve.alias['skiko.mjs'] = skikoInfo.path;
  
  // 方法3: 添加 resolve.modules 来帮助查找
  config.resolve.modules.push(skikoInfo.dir);
  config.resolve.modules.push(buildJsPath);
  
  // 方法4: 使用 CopyWebpackPlugin 将 skiko.mjs 复制到所有测试包的 kotlin 目录
  // 这样可以确保相对路径导入能够工作
  const packagesPath = path.join(buildJsPath, 'packages');
  if (fs.existsSync(packagesPath)) {
    const packages = fs.readdirSync(packagesPath);
    const testPackages = packages.filter(pkg => pkg.includes('test') || pkg.includes('Test'));
    
    const copyPatterns = testPackages.map(pkg => {
      const kotlinDir = path.join(packagesPath, pkg, 'kotlin');
      return {
        from: skikoInfo.path,
        to: path.join(kotlinDir, 'skiko.mjs'),
        noErrorOnMissing: true,
        force: false // 不覆盖已存在的文件
      };
    }).filter(pattern => {
      // 只复制到存在的目录
      const targetDir = path.dirname(pattern.to);
      return fs.existsSync(targetDir);
    });
    
    if (copyPatterns.length > 0) {
      config.plugins.push(
        new CopyWebpackPlugin({
          patterns: copyPatterns
        })
      );
    }
  }
}

// 配置 fallback
config.resolve.fallback = config.resolve.fallback || {};
config.resolve.fallback['./skiko.mjs'] = false;

