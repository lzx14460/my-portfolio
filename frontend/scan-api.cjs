/* eslint-disable */
const fs = require('fs');
const path = require('path');

class PageApiScanner {
    constructor(rootDir) {
        this.rootDir = rootDir;
        this.pageApis = new Map(); // 存储页面和对应的API
    }

    // 扫描所有 Vue 文件
    scanVueFiles(dir) {
        const files = fs.readdirSync(dir);

        for (const file of files) {
            const fullPath = path.join(dir, file);
            const stat = fs.statSync(fullPath);

            if (stat.isDirectory()) {
                if (!file.includes('node_modules') && !file.includes('dist')) {
                    this.scanVueFiles(fullPath);
                }
            } else if (file.endsWith('.vue')) {
                this.analyzeVueFile(fullPath);
            } else if (file.endsWith('.js') || file.endsWith('.ts')) {
                this.analyzeJsFile(fullPath);
            }
        }
    }

    // 分析 Vue 文件
    analyzeVueFile(filePath) {
        const content = fs.readFileSync(filePath, 'utf-8');
        const pageName = this.getPageName(filePath);
        const apis = this.extractApis(content);

        if (apis.length > 0) {
            this.pageApis.set(pageName, {
                file: filePath,
                apis: apis,
                methods: this.extractMethodNames(content)
            });
        }
    }

    // 分析 JS/TS 文件
    analyzeJsFile(filePath) {
        const content = fs.readFileSync(filePath, 'utf-8');
        const fileName = path.basename(filePath);

        // 只分析 API 相关的文件
        if (fileName.includes('api') || fileName.includes('service') || fileName.includes('request')) {
            const apis = this.extractApis(content);
            if (apis.length > 0) {
                this.pageApis.set(`[API] ${fileName}`, {
                    file: filePath,
                    apis: apis,
                    isService: true
                });
            }
        }
    }

    // 提取 API 调用
    extractApis(content) {
        const apis = [];
        const patterns = [
            // axios 调用
            { pattern: /axios\.(get|post|put|delete|patch)\(['"`]([^'"`]+)['"`]/g, type: 'axios' },
            // 直接调用
            { pattern: /\.(get|post|put|delete)\(['"`]([^'"`]+)['"`]/g, type: 'direct' },
            // fetch
            { pattern: /fetch\(['"`]([^'"`]+)['"`]/g, type: 'fetch' },
            // 封装的 api 调用
            { pattern: /api\.(\w+)\(['"`]([^'"`]+)['"`]/g, type: 'api' },
            // request 调用
            { pattern: /request\(['"`]([^'"`]+)['"`]/g, type: 'request' },
        ];

        for (const {pattern, type} of patterns) {
            let match;
            while ((match = pattern.exec(content)) !== null) {
                let url = match[2] || match[1];
                let method = match[1] || 'GET';

                if (url && (url.includes('/api/') || url.includes('/api'))) {
                    // 清理动态参数
                    url = url.replace(/\$\{.*?\}/g, ':param');
                    url = url.replace(/`.*?\${.*?}`/g, ':param');

                    apis.push({
                        method: method.toUpperCase(),
                        url: url,
                        type: type,
                        line: this.getLineNumber(content, match.index)
                    });
                }
            }
        }

        return [...new Map(apis.map(api => [JSON.stringify(api), api])).values()];
    }

    // 提取 Vue 文件中的方法名
    extractMethodNames(content) {
        const methods = [];
        const methodPattern = /(\w+)\([^)]*\)\s*\{/g;
        let match;
        while ((match = methodPattern.exec(content)) !== null) {
            methods.push(match[1]);
        }
        return methods;
    }

    // 获取页面名称（相对路径）
    getPageName(filePath) {
        const relativePath = path.relative(this.rootDir, filePath);
        return relativePath.replace(/\\/g, '/').replace(/\.vue$/, '');
    }

    // 获取行号
    getLineNumber(content, index) {
        const lines = content.substring(0, index).split('\n');
        return lines.length;
    }

    // 生成报告
    generateReport() {
        let report = '='.repeat(80) + '\n';
        report += '页面与API接口对应关系报告\n';
        report += '生成时间: ' + new Date().toLocaleString() + '\n';
        report += '='.repeat(80) + '\n\n';

        let totalApis = 0;

        for (const [page, info] of this.pageApis) {
            report += `📄 页面: ${page}\n`;
            report += `📍 文件: ${info.file}\n`;

            if (info.methods && info.methods.length > 0) {
                report += `🔧 方法: ${info.methods.join(', ')}\n`;
            }

            report += `🔗 API接口 (${info.apis.length}个):\n`;

            info.apis.forEach((api, idx) => {
                report += `   ${idx + 1}. [${api.method}] ${api.url}\n`;
                totalApis++;
            });

            report += '\n' + '-'.repeat(80) + '\n\n';
        }

        // 生成汇总
        report += '📊 统计汇总:\n';
        report += `   - 总页面数: ${this.pageApis.size}\n`;
        report += `   - 总API数: ${totalApis}\n`;

        // 生成 JSON 格式
        const jsonReport = {
            generatedAt: new Date().toISOString(),
            pages: Object.fromEntries(this.pageApis),
            summary: {
                totalPages: this.pageApis.size,
                totalApis: totalApis
            }
        };

        // 保存报告
        const reportFile = path.join(__dirname, 'page-api-report.txt');
        fs.writeFileSync(reportFile, report);

        const jsonFile = path.join(__dirname, 'page-api-report.json');
        fs.writeFileSync(jsonFile, JSON.stringify(jsonReport, null, 2));

        console.log(`✅ 报告已生成:`);
        console.log(`   - 文本报告: ${reportFile}`);
        console.log(`   - JSON报告: ${jsonFile}`);

        // 输出到控制台
        console.log('\n' + report);
    }
}

// 使用示例
const scanner = new PageApiScanner(path.join(__dirname, 'src'));
console.log('开始扫描页面和API接口...\n');
scanner.scanVueFiles(path.join(__dirname, 'src'));
scanner.generateReport();
