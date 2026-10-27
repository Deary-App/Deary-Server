const fs = require('node:fs');
const path = require('node:path');
const { execFileSync } = require('node:child_process');
process.chdir(path.resolve(__dirname, '..'));
const patterns = [
  ['secret credential', /\b(?:sb_secret_|sbp_|sk-proj-|sk_live_|ghp_|github_pat_)[A-Za-z0-9_-]{16,}/],
  ['private key', /-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----/],
  ['database credential URL', /postgres(?:ql)?:\/\/[^\s:]+:[^\s@]+@/],
];
let files = 0;
const seen = new Set();
const matches = [];
function scan(file) {
  if (!fs.existsSync(file) || seen.has(path.resolve(file))) return;
  seen.add(path.resolve(file));
  if (fs.lstatSync(file).isSymbolicLink()) return;
  if (fs.statSync(file).isDirectory()) {
    if (['.temp', '.branches'].includes(path.basename(file))) return;
    for (const name of fs.readdirSync(file)) scan(path.join(file, name));
    return;
  }
  const bytes = fs.readFileSync(file);
  if (bytes.includes(0)) return;
  files++;
  const text = bytes.toString('utf8');
  for (const [kind, pattern] of patterns) if (pattern.test(text)) matches.push({ file, kind });
  const tokens = text.match(/eyJ[A-Za-z0-9_-]{15,}\.[A-Za-z0-9_-]{15,}\.[A-Za-z0-9_-]{15,}/g) ?? [];
  for (const token of tokens) {
    try { if (JSON.parse(Buffer.from(token.split('.')[1], 'base64url')).role !== 'anon') matches.push({ file, kind: 'non-public JWT' }); }
    catch { matches.push({ file, kind: 'unclassified JWT' }); }
  }
}
// Scan every tracked or addable file, including docs/config and new files.
// Ignored credentials/build output stay local and are not upload candidates.
const candidates = execFileSync('git', ['ls-files', '--cached', '--others', '--exclude-standard', '-z'], { encoding: 'utf8' }).split('\0').filter(Boolean);
for (const file of candidates) scan(file);
if (process.argv[2]) scan(process.argv[2]);
console.log(JSON.stringify({ files, matches, note: 'Pattern scan only; matching secret values are never printed.' }, null, 2));
if (matches.length) process.exitCode = 1;
