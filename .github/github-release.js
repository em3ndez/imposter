const fs = require('fs');

module.exports = async ({github, context}) => {
    if (!context.ref.match(/refs\/tags\/.+/)) {
        console.warn(`Unsupported ref: ${context.ref}`);
        return;
    }
    const releaseVersion = context.ref.split('/')[2];
    if (!releaseVersion) {
        console.warn('No release version - aborting');
        return;
    }

    console.log(`Creating release: ${releaseVersion}`);
    const release = await github.rest.repos.createRelease({
        owner: 'outofcoffee',
        repo: 'imposter',
        tag_name: releaseVersion,
        body: 'See [change log](https://github.com/outofcoffee/imposter/blob/master/CHANGELOG.md)',
    });

    const localFilePath = './distro/all/build/libs/imposter-all.jar';
    await uploadAsset(github, release.data.id, 'imposter.jar', localFilePath, release.data.id);

    // upload with version suffix, for compatibility with cli < 0.7.0
    const numericVersion = releaseVersion.startsWith('v') ? releaseVersion.substr(1) : releaseVersion;
    await uploadAsset(github, release.data.id, `imposter-${numericVersion}.jar`, localFilePath, release.data.id);

    console.log(`Assets uploaded to release: ${releaseVersion}`);
};

async function uploadAsset(github, releaseId, assetFileName, localFilePath) {
    console.log(`Uploading ${localFilePath} as release asset ${assetFileName}...`);
    await github.rest.repos.uploadReleaseAsset({
        owner: 'outofcoffee',
        repo: 'imposter',
        release_id: releaseId,
        name: assetFileName,
        data: await fs.promises.readFile(localFilePath),
    });
}
