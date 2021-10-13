const fs = require('fs')
const path = require('path')

function findFile(dir, regexp) {
    if(!fs.existsSync(dir)) return [];
    const result = [];
    for (let d of fs.readdirSync(dir)) {
        const file = path.join(dir, d);
        const fileStat = fs.statSync(file);
        if(fileStat.isFile()){
            if(d.match(regexp)) result.push(file);
        }else{
            result.push(...findFile(file, regexp))
        }
    }
    return result;
}
const resourcesDir = "src/main/resources";
process.chdir(resourcesDir);
const htmlList = findFile("./", /\.html$/).map(p=>"../" + p);
process.chdir("../../../");

module.exports = {
    context: path.resolve(__dirname, "src/main/resources/static"),
    entry: htmlList,
    mode: "development",
    output: {
        path: path.resolve(__dirname, "target/classes/static"),
        clean: true
    },
    module: {
        rules: [
            {
                test: /\.html$/i,
                type: "asset/resource",
                generator: {
                    filename: "[name][ext]"
                }
            },
            {
                test: /\.html$/i,
                use: ['extract-loader', 'html-loader']
            }
        ]
    }
}